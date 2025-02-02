package mil.nga.geopackage.test.features.user;

import java.sql.SQLException;

import mil.nga.geopackage.test.ImportGeoPackageTestCase;

import org.junit.Test;

/**
 * Test Features from an imported database
 * 
 * @author osbornb
 */
public class FeatureImportTest extends ImportGeoPackageTestCase {

	/**
	 * Constructor
	 */
	public FeatureImportTest() {

	}

	/**
	 * Test reading
	 * 
	 * @throws SQLException
	 *             upon error
	 */
	@Test
	public void testRead() throws SQLException {

		FeatureUtils.testRead(geoPackage);

	}

	/**
	 * Test updating
	 * 
	 * @throws SQLException
	 *             upon error
	 */
	@Test
	public void testUpdate() throws SQLException {

		FeatureUtils.testUpdate(geoPackage);

	}

	/**
	 * Test updating with added columns
	 * 
	 * @throws SQLException
	 *             upon error
	 */
	@Test
	public void testUpdateAddColumns() throws SQLException {

		FeatureUtils.testUpdateAddColumns(geoPackage);

	}

	/**
	 * Test creating
	 * 
	 * @throws SQLException
	 *             upon error
	 */
	@Test
	public void testCreate() throws SQLException {

		FeatureUtils.testCreate(geoPackage);

	}

	/**
	 * Test deleting
	 * 
	 * @throws SQLException
	 *             upon error
	 */
	@Test
	public void testDelete() throws SQLException {

		FeatureUtils.testDelete(geoPackage);

	}

}
