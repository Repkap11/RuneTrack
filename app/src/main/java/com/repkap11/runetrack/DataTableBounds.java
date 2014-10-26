package com.repkap11.runetrack;

public class DataTableBounds {
public int total;
public int[] totals;
public int imageSize;
public int width;
public float textSize;

public DataTableBounds(int imageSize, int[] totals, int total, int width, float textSize) {
	this.total = total;
	this.totals = totals;
	this.imageSize = imageSize;
	this.width = width;
	this.textSize = textSize;
}
}