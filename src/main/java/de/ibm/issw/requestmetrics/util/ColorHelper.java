package de.ibm.issw.requestmetrics.util;

import java.awt.Color;
import java.util.Random;

public class ColorHelper {

	/**
	 * This method generates a random color. Mixing random colors with white
	 * (255, 255, 255) creates neutral pastels by increasing the lightness while
	 * keeping the hue of the original color. These randomly generated pastels
	 * usually go well together, especially in large numbers.
	 * 
	 * @param mix
	 *            the color to mix with
	 * @return a randomly generated mixed color
	 */
	public static Color generateRandomColor(Color mix) {
		Random random = new Random();
		int red = random.nextInt(256);
		int green = random.nextInt(256);
		int blue = random.nextInt(256);

		Color color = new Color(red, green, blue);

		return getPastelColor(color, mix);
	}
	
	/**
	 * Mixes a color with another color to e.g. get pastel colors when mixing with white.
	 * 
	 * @param color		original color
	 * @param mix		color to mix with
	 * @return			mixed color
	 */
	public static Color getPastelColor(Color color, Color mix) {
		if (mix != null && color != null) {
			int red = (color.getRed() + mix.getRed()) / 2;
			int green = (color.getGreen() + mix.getGreen()) / 2;
			int blue = (color.getBlue() + mix.getBlue()) / 2;
			color = new Color(red, green, blue);
		}
		return color;
	}
}
