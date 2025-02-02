package mil.nga.geopackage.features.user;

import java.util.ArrayList;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.features.index.FeatureIndexManager;
import mil.nga.sf.GeometryEnvelope;
import mil.nga.sf.proj.Projection;
import mil.nga.sf.proj.ProjectionTransform;

/**
 * Performs manual brute force queries against feature rows. See
 * {@link FeatureIndexManager} for performing indexed queries.
 * 
 * @author osbornb
 * @since 3.1.0
 */
public class ManualFeatureQuery {

	/**
	 * Feature DAO
	 */
	private final FeatureDao featureDao;

	/**
	 * Query single chunk limit
	 */
	protected int chunkLimit = 1000;

	/**
	 * Query range tolerance
	 */
	protected double tolerance = .00000000000001;

	/**
	 * Constructor
	 *
	 * @param featureDao
	 *            feature DAO
	 */
	public ManualFeatureQuery(FeatureDao featureDao) {
		this.featureDao = featureDao;
	}

	/**
	 * Get the feature DAO
	 * 
	 * @return feature DAO
	 */
	public FeatureDao getFeatureDao() {
		return featureDao;
	}

	/**
	 * Get the SQL query chunk limit
	 * 
	 * @return chunk limit
	 */
	public int getChunkLimit() {
		return chunkLimit;
	}

	/**
	 * Set the SQL query chunk limit
	 * 
	 * @param chunkLimit
	 *            chunk limit
	 */
	public void setChunkLimit(int chunkLimit) {
		this.chunkLimit = chunkLimit;
	}

	/**
	 * Get the query range tolerance
	 * 
	 * @return query range tolerance
	 */
	public double getTolerance() {
		return tolerance;
	}

	/**
	 * Set the query range tolerance
	 * 
	 * @param tolerance
	 *            query range tolerance
	 */
	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}

	/**
	 * Get the count of features
	 *
	 * @return count
	 */
	public int count() {
		return featureDao.count();
	}

	/**
	 * Get the count of features with non null geometries
	 *
	 * @return count
	 */
	public int countWithGeometries() {
		return featureDao.count(featureDao.getGeometryColumnName()
				+ " IS NOT NULL", null);
	}

	/**
	 * Manually build the bounds of the feature table
	 * 
	 * @return bounding box
	 */
	public BoundingBox getBoundingBox() {

		GeometryEnvelope envelope = null;

		long offset = 0;
		boolean hasResults = true;

		while (hasResults) {

			hasResults = false;

			FeatureResultSet resultSet = featureDao.queryForChunk(chunkLimit,
					offset);
			try {
				while (resultSet.moveToNext()) {
					hasResults = true;

					FeatureRow featureRow = resultSet.getRow();
					GeometryEnvelope featureEnvelope = featureRow
							.getGeometryEnvelope();
					if (featureEnvelope != null) {

						if (envelope == null) {
							envelope = featureEnvelope;
						} else {
							envelope = envelope.union(featureEnvelope);
						}

					}
				}
			} finally {
				resultSet.close();
			}

			offset += chunkLimit;
		}

		BoundingBox boundingBox = null;
		if (envelope != null) {
			boundingBox = new BoundingBox(envelope);
		}

		return boundingBox;
	}

	/**
	 * Manually build the bounds of the feature table in the provided projection
	 * 
	 * @param projection
	 *            desired projection
	 * @return bounding box
	 */
	public BoundingBox getBoundingBox(Projection projection) {
		BoundingBox boundingBox = getBoundingBox();
		if (boundingBox != null && projection != null) {
			ProjectionTransform projectionTransform = featureDao
					.getProjection().getTransformation(projection);
			boundingBox = boundingBox.transform(projectionTransform);
		}
		return boundingBox;
	}

	/**
	 * Manually query for rows within the bounding box
	 * 
	 * @param boundingBox
	 *            bounding box
	 * @return results
	 */
	public ManualFeatureQueryResults query(BoundingBox boundingBox) {
		return query(boundingBox.buildEnvelope());
	}

	/**
	 * Manually query for rows within the bounding box in the provided
	 * projection
	 * 
	 * @param boundingBox
	 *            bounding box
	 * @param projection
	 *            projection
	 * @return results
	 */
	public ManualFeatureQueryResults query(BoundingBox boundingBox,
			Projection projection) {
		BoundingBox featureBoundingBox = featureDao.projectBoundingBox(
				boundingBox, projection);
		return query(featureBoundingBox);
	}

	/**
	 * Manually count the rows within the bounding box
	 * 
	 * @param boundingBox
	 *            bounding box
	 * @return count
	 */
	public long count(BoundingBox boundingBox) {
		return count(boundingBox.buildEnvelope());
	}

	/**
	 * Manually count the rows within the bounding box in the provided
	 * projection
	 * 
	 * @param boundingBox
	 *            bounding box
	 * @param projection
	 *            projection
	 * @return count
	 */
	public long count(BoundingBox boundingBox, Projection projection) {
		BoundingBox featureBoundingBox = featureDao.projectBoundingBox(
				boundingBox, projection);
		return count(featureBoundingBox);
	}

	/**
	 * Manually query for rows within the geometry envelope
	 * 
	 * @param envelope
	 *            geometry envelope
	 * @return results
	 */
	public ManualFeatureQueryResults query(GeometryEnvelope envelope) {
		return query(envelope.getMinX(), envelope.getMinY(),
				envelope.getMaxX(), envelope.getMaxY());
	}

	/**
	 * Manually count the rows within the geometry envelope
	 * 
	 * @param envelope
	 *            geometry envelope
	 * @return count
	 */
	public long count(GeometryEnvelope envelope) {
		return count(envelope.getMinX(), envelope.getMinY(),
				envelope.getMaxX(), envelope.getMaxY());
	}

	/**
	 * Manually query for rows within the bounds
	 * 
	 * @param minX
	 *            min x
	 * @param minY
	 *            min y
	 * @param maxX
	 *            max x
	 * @param maxY
	 *            max y
	 * @return results
	 */
	public ManualFeatureQueryResults query(double minX, double minY,
			double maxX, double maxY) {

		List<Long> featureIds = new ArrayList<>();

		long offset = 0;
		boolean hasResults = true;

		minX -= tolerance;
		maxX += tolerance;
		minY -= tolerance;
		maxY += tolerance;

		while (hasResults) {

			hasResults = false;

			FeatureResultSet resultSet = featureDao.queryForChunk(chunkLimit,
					offset);
			try {
				while (resultSet.moveToNext()) {
					hasResults = true;

					FeatureRow featureRow = resultSet.getRow();
					GeometryEnvelope envelope = featureRow
							.getGeometryEnvelope();
					if (envelope != null) {

						double minXMax = Math.max(minX, envelope.getMinX());
						double maxXMin = Math.min(maxX, envelope.getMaxX());
						double minYMax = Math.max(minY, envelope.getMinY());
						double maxYMin = Math.min(maxY, envelope.getMaxY());

						if (minXMax <= maxXMin && minYMax <= maxYMin) {
							featureIds.add(featureRow.getId());
						}

					}
				}
			} finally {
				resultSet.close();
			}

			offset += chunkLimit;
		}

		ManualFeatureQueryResults results = new ManualFeatureQueryResults(
				featureDao, featureIds);

		return results;
	}

	/**
	 * Manually count the rows within the bounds
	 * 
	 * @param minX
	 *            min x
	 * @param minY
	 *            min y
	 * @param maxX
	 *            max x
	 * @param maxY
	 *            max y
	 * @return count
	 */
	public long count(double minX, double minY, double maxX, double maxY) {
		return query(minX, minY, maxX, maxY).count();
	}

}
