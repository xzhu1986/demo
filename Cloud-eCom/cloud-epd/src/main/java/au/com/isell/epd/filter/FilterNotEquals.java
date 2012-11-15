/*
 * Copyright (c) 2007-2010 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
 * Cascading is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cascading is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cascading.  If not, see <http://www.gnu.org/licenses/>.
 */

package au.com.isell.epd.filter;

import java.beans.ConstructorProperties;

import cascading.flow.FlowProcess;
import cascading.operation.BaseOperation;
import cascading.operation.Filter;
import cascading.operation.FilterCall;
import cascading.tuple.Tuple;

/**
 * Class FilterNull verifies that every value in the argument values
 * {@link cascading.tuple.Tuple} is not a null value. If a null value is
 * encountered, the current Tuple will be filtered out.
 * 
 * @see FilterNotNull
 */
public class FilterNotEquals extends BaseOperation implements Filter {
	private static final long serialVersionUID = 1L;
	private Tuple values;

	/**
	 * Constructor AssertEquals creates a new AssertEquals instance.
	 * 
	 * @param values
	 *            of type Object...
	 */
	@ConstructorProperties({ "values" })
	public FilterNotEquals(Object... values) {
		if (values == null)
			throw new IllegalArgumentException("values may not be null");

		if (values.length == 0)
			throw new IllegalArgumentException("values may not be empty");

		this.values = new Tuple(values);
	}

	public boolean isRemove(FlowProcess flowProcess, FilterCall filterCall) {
		Tuple tuple = filterCall.getArguments().getTuple();
		if (!tuple.equals(values))
			return true;
		return false;
	}
}