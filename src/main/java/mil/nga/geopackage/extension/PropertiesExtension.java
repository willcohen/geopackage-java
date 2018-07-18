package mil.nga.geopackage.extension;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.attributes.AttributesDao;
import mil.nga.geopackage.attributes.AttributesResultSet;
import mil.nga.geopackage.attributes.AttributesRow;

/**
 * GeoPackage properties extension for defining GeoPackage specific properties,
 * attributes, and metadata
 * 
 * @author osbornb
 * @since 3.0.2
 */
public class PropertiesExtension
		extends
		PropertiesCoreExtension<AttributesRow, AttributesResultSet, AttributesDao> {

	private final GeoPackage geoPackage;

	/**
	 * Constructor
	 * 
	 * @param geoPackage
	 *            GeoPackage
	 */
	public PropertiesExtension(GeoPackage geoPackage) {
		super(geoPackage);
		this.geoPackage = geoPackage;
	}

	@Override
	protected AttributesDao getDao() {
		return geoPackage.getAttributesDao(TABLE_NAME);
	}

	@Override
	protected AttributesRow newRow() {
		return getDao().newRow();
	}

}
