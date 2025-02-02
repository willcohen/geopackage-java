package mil.nga.geopackage.tiles.user;

import java.awt.image.BufferedImage;
import java.io.IOException;

import mil.nga.geopackage.tiles.ImageUtils;
import mil.nga.geopackage.user.UserRow;

/**
 * Tile Row containing the values from a single cursor row
 * 
 * @author osbornb
 */
public class TileRow extends UserRow<TileColumn, TileTable> {

	/**
	 * Constructor
	 * 
	 * @param table
	 * @param columnTypes
	 * @param values
	 */
	TileRow(TileTable table, int[] columnTypes, Object[] values) {
		super(table, columnTypes, values);
	}

	/**
	 * Constructor to create an empty row
	 * 
	 * @param table
	 */
	TileRow(TileTable table) {
		super(table);
	}

	/**
	 * Copy Constructor
	 * 
	 * @param tileRow
	 *            tile row to copy
	 * @since 1.3.0
	 */
	public TileRow(TileRow tileRow) {
		super(tileRow);
	}

	/**
	 * Get the zoom level column index
	 * 
	 * @return zoom level column index
	 */
	public int getZoomLevelColumnIndex() {
		return getTable().getZoomLevelColumnIndex();
	}

	/**
	 * Get the zoom level column
	 * 
	 * @return zoom level column
	 */
	public TileColumn getZoomLevelColumn() {
		return getTable().getZoomLevelColumn();
	}

	/**
	 * Get the zoom level
	 * 
	 * @return zoom level
	 */
	public long getZoomLevel() {
		return ((Number) getValue(getZoomLevelColumnIndex())).longValue();
	}

	/**
	 * Set the zoom level
	 * 
	 * @param zoomLevel
	 *            zoom level
	 */
	public void setZoomLevel(long zoomLevel) {
		setValue(getZoomLevelColumnIndex(), zoomLevel);
	}

	/**
	 * Get the tile column column index
	 * 
	 * @return tile column index
	 */
	public int getTileColumnColumnIndex() {
		return getTable().getTileColumnColumnIndex();
	}

	/**
	 * Get the tile column column
	 * 
	 * @return tile column
	 */
	public TileColumn getTileColumnColumn() {
		return getTable().getTileColumnColumn();
	}

	/**
	 * Get the tile column
	 * 
	 * @return tile column
	 */
	public long getTileColumn() {
		return ((Number) getValue(getTileColumnColumnIndex())).longValue();
	}

	/**
	 * Set the tile column
	 * 
	 * @param tileColumn
	 *            tile column
	 */
	public void setTileColumn(long tileColumn) {
		setValue(getTileColumnColumnIndex(), tileColumn);
	}

	/**
	 * Get the tile row column index
	 * 
	 * @return tile row column index
	 */
	public int getTileRowColumnIndex() {
		return getTable().getTileRowColumnIndex();
	}

	/**
	 * Get the tile row column
	 * 
	 * @return tile row column
	 */
	public TileColumn getTileRowColumn() {
		return getTable().getTileRowColumn();
	}

	/**
	 * Get the tile row
	 * 
	 * @return tile row
	 */
	public long getTileRow() {
		return ((Number) getValue(getTileRowColumnIndex())).longValue();
	}

	/**
	 * Set the tile row
	 * 
	 * @param tileRow
	 *            tile row
	 */
	public void setTileRow(long tileRow) {
		setValue(getTileRowColumnIndex(), tileRow);
	}

	/**
	 * Get the tile data column index
	 * 
	 * @return tile data column index
	 */
	public int getTileDataColumnIndex() {
		return getTable().getTileDataColumnIndex();
	}

	/**
	 * Get the tile data column
	 * 
	 * @return tile data column
	 */
	public TileColumn getTileDataColumn() {
		return getTable().getTileDataColumn();
	}

	/**
	 * Get the tile data
	 * 
	 * @return bytes
	 */
	public byte[] getTileData() {
		return (byte[]) getValue(getTileDataColumnIndex());
	}

	/**
	 * Set the tile data
	 * 
	 * @param tileData
	 *            tile data
	 */
	public void setTileData(byte[] tileData) {
		setValue(getTileDataColumnIndex(), tileData);
	}

	/**
	 * Get the tile data image
	 * 
	 * @return image
	 * @throws IOException
	 *             upon failure
	 */
	public BufferedImage getTileDataImage() throws IOException {
		BufferedImage image = ImageUtils.getImage(getTileData());
		return image;
	}

	/**
	 * Set the tile data from an image
	 * 
	 * @param image
	 *            image
	 * @param imageFormat
	 *            image format
	 * @throws IOException
	 *             upon failure
	 */
	public void setTileData(BufferedImage image, String imageFormat)
			throws IOException {
		byte[] bytes = ImageUtils.writeImageToBytes(image, imageFormat);
		setTileData(bytes);
	}

	/**
	 * Copy the row
	 * 
	 * @return row copy
	 * @since 3.0.1
	 */
	public TileRow copy() {
		return new TileRow(this);
	}

}
