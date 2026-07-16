package com.ikunkk02afk.hookandreel.fishing;

public final class FishingEntityCategoryWeights {
	private FishingEntityCategoryWeights() {
	}

	public static int selectIndex(double[] weights, double unitRoll) {
		double total = 0.0D;
		for (double weight : weights) {
			if (isPositiveFinite(weight)) {
				total += weight;
			}
		}
		if (!(total > 0.0D) || !Double.isFinite(total)) {
			return -1;
		}

		double normalizedRoll = Double.isFinite(unitRoll)
			? Math.clamp(unitRoll, 0.0D, Math.nextDown(1.0D))
			: 0.0D;
		double target = normalizedRoll * total;
		double cumulative = 0.0D;
		int lastPositive = -1;
		for (int index = 0; index < weights.length; index++) {
			double weight = weights[index];
			if (!isPositiveFinite(weight)) {
				continue;
			}
			lastPositive = index;
			cumulative += weight;
			if (target < cumulative) {
				return index;
			}
		}
		return lastPositive;
	}

	public static boolean isPositiveFinite(double value) {
		return Double.isFinite(value) && value > 0.0D;
	}
}
