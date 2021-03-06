/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.dynamic.data.mapping.type.checkbox;

import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portlet.dynamicdatamapping.model.LocalizedValue;
import com.liferay.portlet.dynamicdatamapping.model.UnlocalizedValue;
import com.liferay.portlet.dynamicdatamapping.model.Value;
import com.liferay.portlet.dynamicdatamapping.storage.DDMFormFieldValue;
import com.liferay.portlet.dynamicdatamapping.util.test.DDMFormValuesTestUtil;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Renato Rego
 */
public class CheckboxDDMFormFieldValueAccessorTest {

	@Test
	public void testGetWithLocalizedValue() {
		Value value = new LocalizedValue(LocaleUtil.US);

		value.addString(LocaleUtil.BRAZIL, "true");
		value.addString(LocaleUtil.US, "false");

		DDMFormFieldValue ddmFormFieldValue =
			DDMFormValuesTestUtil.createDDMFormFieldValue("Checkbox", value);

		CheckboxDDMFormFieldValueAccessor checkboxDDMFormFieldValueAccessor =
			new CheckboxDDMFormFieldValueAccessor(LocaleUtil.BRAZIL);

		Assert.assertEquals(
			Boolean.TRUE,
			checkboxDDMFormFieldValueAccessor.get(ddmFormFieldValue));

		checkboxDDMFormFieldValueAccessor =
			new CheckboxDDMFormFieldValueAccessor(LocaleUtil.US);

		Assert.assertEquals(
			Boolean.FALSE,
			checkboxDDMFormFieldValueAccessor.get(ddmFormFieldValue));
	}

	@Test
	public void testGetWithUnlocalizedValue() {
		DDMFormFieldValue ddmFormFieldValue =
			DDMFormValuesTestUtil.createDDMFormFieldValue(
				"Checkbox", new UnlocalizedValue("true"));

		CheckboxDDMFormFieldValueAccessor checkboxDDMFormFieldValueAccessor =
			new CheckboxDDMFormFieldValueAccessor(LocaleUtil.US);

		Assert.assertEquals(
			Boolean.TRUE,
			checkboxDDMFormFieldValueAccessor.get(ddmFormFieldValue));

		ddmFormFieldValue.setValue(new UnlocalizedValue("false"));

		Assert.assertEquals(
			Boolean.FALSE,
			checkboxDDMFormFieldValueAccessor.get(ddmFormFieldValue));
	}

}