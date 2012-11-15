package au.com.isell.epd.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * the calculation rule which can convert an value in base unit to this unit.
 * Algorithm:
 * 
 * Output = ROUND ( ( ( SV + PRECA) / CF / 10^SCALE) + POSTCA, PREC)
 * 
 * Where
 * 
 * SV = Source Value (Integer) PRECA = Pre Conversion Adjustment (Signed
 * Decimal) SCALE = Power of 10 Scale (Signed Integer) CF = Conversion Factor
 * Multiplier (Signed Decimal) POSTCA = Post Conversion Adjustment (Signed
 * Decimal) PREC = Precision (Integer) is used to round the output value to PREC
 * decimal places
 * 
 * 
 */
public class UnitUtils {
	public static BigDecimal convertFromBaseUnit(BigDecimal baseValue,BigDecimal preca,BigDecimal cf,int scale,BigDecimal postca,int prec) {
		if (baseValue == null) return null;
		return baseValue.add(preca).divide(cf,prec+1,RoundingMode.HALF_UP).divide(new BigDecimal(10).pow(scale),prec+1,RoundingMode.HALF_UP).add(postca).setScale(prec, RoundingMode.HALF_UP);
	}

	public static BigDecimal convertToBaseValue(BigDecimal value,BigDecimal preca,BigDecimal cf,int scale,BigDecimal postca) {
		if (value == null) return null;
		return value.subtract(postca).multiply(new BigDecimal(10).pow(scale)).multiply(cf).subtract(preca);
	}
}
