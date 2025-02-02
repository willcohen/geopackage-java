package mil.nga.geopackage.extension.index;

import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageException;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureResultSet;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureRowSync;
import mil.nga.sf.proj.Projection;

import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Feature Table Index NGA Extension implementation. This extension is used to
 * index Geometries within a feature table by their minimum bounding box for
 * bounding box queries. This extension is required to provide an index
 * implementation when a SQLite version is used before SpatialLite support
 * (Android).
 * 
 * @author osbornb
 * @since 1.1.0
 */
public class FeatureTableIndex extends FeatureTableCoreIndex {

	/**
	 * Logger
	 */
	private static final Logger log = Logger.getLogger(FeatureTableIndex.class
			.getName());

	/**
	 * Feature DAO
	 */
	private final FeatureDao featureDao;

	/**
	 * Feature Row Sync for simultaneous same row queries
	 */
	private final FeatureRowSync featureRowSync = new FeatureRowSync();

	/**
	 * Constructor
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 * @param featureDao
	 *            feature dao
	 */
	public FeatureTableIndex(GeoPackage geoPackage, FeatureDao featureDao) {
		super(geoPackage, featureDao.getTableName(), featureDao
				.getGeometryColumnName());
		this.featureDao = featureDao;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Projection getProjection() {
		return featureDao.getProjection();
	}

	/**
	 * Close the table index
	 */
	public void close() {
		// Don't close anything, leave the GeoPackage connection open
	}

	/**
	 * Index the feature row. This method assumes that indexing has been
	 * completed and maintained as the last indexed time is updated.
	 *
	 * @param row
	 *            feature row
	 * @return true if indexed
	 */
	public boolean index(FeatureRow row) {
		TableIndex tableIndex = getTableIndex();
		if (tableIndex == null) {
			throw new GeoPackageException(
					"GeoPackage table is not indexed. GeoPackage: "
							+ getGeoPackage().getName() + ", Table: "
							+ getTableName());
		}
		boolean indexed = index(tableIndex, row.getId(), row.getGeometry());

		// Update the last indexed time
		updateLastIndexed();

		return indexed;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected int indexTable(final TableIndex tableIndex) {

		int count = 0;

		long offset = 0;
		int chunkCount = 0;

		while (chunkCount >= 0) {

			final long chunkOffset = offset;

			try {
				// Iterate through each row and index as a single transaction
				ConnectionSource connectionSource = getGeoPackage()
						.getDatabase().getConnectionSource();
				chunkCount = TransactionManager.callInTransaction(
						connectionSource, new Callable<Integer>() {
							public Integer call() throws Exception {

								FeatureResultSet resultSet = featureDao
										.queryForChunk(chunkLimit, chunkOffset);
								int count = indexRows(tableIndex, resultSet);

								return count;
							}
						});
				if (chunkCount > 0) {
					count += chunkCount;
				}
			} catch (SQLException e) {
				throw new GeoPackageException(
						"Failed to Index Table. GeoPackage: "
								+ getGeoPackage().getName() + ", Table: "
								+ getTableName(), e);
			}

			offset += chunkLimit;
		}

		// Update the last indexed time
		if (progress == null || progress.isActive()) {
			updateLastIndexed();
		}

		return count;
	}

	/**
	 * Index the feature rows in the cursor
	 * 
	 * @param tableIndex
	 *            table index
	 * @param resultSet
	 *            feature result
	 * @return count, -1 if no results or canceled
	 */
	private int indexRows(TableIndex tableIndex, FeatureResultSet resultSet) {

		int count = -1;

		try {
			while ((progress == null || progress.isActive())
					&& resultSet.moveToNext()) {
				if (count < 0) {
					count++;
				}
				try {
					FeatureRow row = resultSet.getRow();
					boolean indexed = index(tableIndex, row.getId(),
							row.getGeometry());
					if (indexed) {
						count++;
					}
					if (progress != null) {
						progress.addProgress(1);
					}
				} catch (Exception e) {
					log.log(Level.SEVERE, "Failed to index feature. Table: "
							+ tableIndex.getTableName() + ", Position: "
							+ resultSet.getPosition(), e);
				}
			}
		} finally {
			resultSet.close();
		}

		return count;
	}

	/**
	 * Delete the index for the feature row
	 *
	 * @param row
	 *            feature row
	 * @return deleted rows, should be 0 or 1
	 */
	public int deleteIndex(FeatureRow row) {
		return deleteIndex(row.getId());
	}

	/**
	 * Get the feature row for the Geometry Index
	 * 
	 * @param geometryIndex
	 *            geometry index
	 * @return feature row
	 */
	public FeatureRow getFeatureRow(GeometryIndex geometryIndex) {

		long geomId = geometryIndex.getGeomId();

		// Get the row or lock for reading
		FeatureRow row = featureRowSync.getRowOrLock(geomId);
		if (row == null) {
			// Query for the row and set in the sync
			try {
				row = featureDao.queryForIdRow(geomId);
			} finally {
				featureRowSync.setRow(geomId, row);
			}
		}

		return row;
	}

}
