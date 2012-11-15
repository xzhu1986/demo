package au.com.isell.common.exi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;

import com.siemens.ct.exi.exceptions.EXIException;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.AbstractDriver;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.xml.QNameMap;

public class EXIDriver extends AbstractDriver implements
		HierarchicalStreamDriver {
	private QNameMap qnameMap = new QNameMap();
	@Override
	public HierarchicalStreamReader createReader(Reader in) {
		throw new UnsupportedOperationException(
				"The EXIDriver cannot use character-oriented input streams.");
	}

	@Override
	public HierarchicalStreamReader createReader(InputStream in) {
		try {
			return new EXIStreamReader(qnameMap, in);
		} catch (EXIException e) {
			throw new StreamException(e);
		} catch (IOException e) {
			throw new StreamException(e);
		} catch (XMLStreamException e) {
			throw new StreamException(e);
		}
	}

	@Override
	public HierarchicalStreamWriter createWriter(Writer out) {
		throw new UnsupportedOperationException(
				"The EXIDriver cannot use character-oriented output streams.");
	}

	@Override
	public HierarchicalStreamWriter createWriter(OutputStream out) {
		try {
			return new EXIStreamWriter(qnameMap, out);
		} catch (EXIException e) {
			throw new StreamException(e);
		} catch (IOException e) {
			throw new StreamException(e);
		} catch (XMLStreamException e) {
			throw new StreamException(e);
		}
	}

}
