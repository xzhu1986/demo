package au.com.isell.epd.utils;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.UUID;

import au.com.isell.common.data.CSVReader;
import au.com.isell.epd.function.ProductIdSearcher;
import au.com.isell.epd.pojo.ProductIdMapping;

public class IndexProdIds {

	private static final int BATCH_SIZE = 500;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Reader r = null;
		try {
			ProductIdSearcher.register();
			r = new FileReader(args[0]);
			CSVReader csv = new CSVReader(r, '\t', '"', false, false);
			int count = 0;
			ProductIdMapping[] mappings = new ProductIdMapping[IndexProdIds.BATCH_SIZE];
			for (String[] line = csv.readLine(); line != null; line = csv.readLine()) {
				mappings[count] = new ProductIdMapping();
				mappings[count].setProductUUID(UUID.fromString(line[0]));
				mappings[count].setCnID(Integer.parseInt(line[1]));
				mappings[count].setUkID(Integer.parseInt(line[2]));
				mappings[count].setUnspsc(Integer.parseInt(line[3]));
				mappings[count].setManufacturer(line[4]);
				mappings[count].setPartNo(line[5]);
				count++;
				if (count % IndexProdIds.BATCH_SIZE == 0) {
					ProductIdSearcher.indexObject(mappings);
					Arrays.fill(mappings, null);
					count = 0;
				}
			}
			if (count > 0) ProductIdSearcher.indexObject(Arrays.copyOf(mappings, count));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (r != null) {
				try {
					r.close();
				} catch (Exception ex) {}
			}
		}
	}

}
