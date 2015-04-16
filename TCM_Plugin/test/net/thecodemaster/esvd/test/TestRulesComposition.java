package net.thecodemaster.esvd.test;

import java.util.List;

import net.thecodemaster.esvd.helper.HelperVerifiers;
import net.thecodemaster.esvd.ui.enumeration.EnumRules;

import org.junit.Assert;
import org.junit.Test;

public class TestRulesComposition {

	private List<EnumRules> getRulesFromValue(int rules) {
		return HelperVerifiers.getRulesFromValue(rules);
	}

	@Test
	public void test01() {
		List<EnumRules> listRules = getRulesFromValue(1);
		Assert.assertEquals(1, listRules.size());
	}

	@Test
	public void test02() {
		List<EnumRules> listRules = getRulesFromValue(2);
		Assert.assertEquals(1, listRules.size());
	}

	@Test
	public void test04() {
		List<EnumRules> listRules = getRulesFromValue(4);
		Assert.assertEquals(1, listRules.size());
	}

	@Test
	public void test06() {
		List<EnumRules> listRules = getRulesFromValue(6);
		Assert.assertEquals(2, listRules.size());
	}

	@Test
	public void test08() {
		List<EnumRules> listRules = getRulesFromValue(8);
		Assert.assertEquals(1, listRules.size());
	}

	@Test
	public void test10() {
		List<EnumRules> listRules = getRulesFromValue(10);
		Assert.assertEquals(2, listRules.size());
	}

	@Test
	public void test12() {
		List<EnumRules> listRules = getRulesFromValue(12);
		Assert.assertEquals(2, listRules.size());
	}

	@Test
	public void test14() {
		List<EnumRules> listRules = getRulesFromValue(14);
		Assert.assertEquals(3, listRules.size());
	}

}
