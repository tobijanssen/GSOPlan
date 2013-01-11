/*
 * LinearLayoutBordered.java
 * 
 * Tobias Janssen, 2013
 * GNU GENERAL PUBLIC LICENSE Version 2
 */
package de.janssen.android.gsoplan.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.LinearLayout;



public class LinearLayoutBordered extends LinearLayout {
	private boolean borderLeft = true;
	private boolean borderRight = true;
	private boolean borderTop = true;
	private boolean borderBottom = true;
	
	private int borderSize = 2;
	
	private Paint p = null;
	
	public LinearLayoutBordered(Context context) {
		super(context);
		setWillNotDraw(false);
			
	}
	
	public boolean isBorderLeft() {
		return borderLeft;
	}
	
	public void setBorderLeft(boolean borderLeft) {
		this.borderLeft = borderLeft;
	}
	
	public boolean isBorderRight() {
		return borderRight;
	}

	public void setBorderRight(boolean borderRight) {
		this.borderRight = borderRight;
	}
	
	public boolean isBorderTop() {
		return borderTop;
	}
	
	public void setBorderTop(boolean borderTop) {
		this.borderTop = borderTop;
	}
	
	public boolean isBorderBottom() {
		return borderBottom;
	}
	
	public void setBorderBottom(boolean borderBottom) {
		this.borderBottom = borderBottom;
	}
	
	public Paint getPaint() {
		if(p == null) 
		{
			p = new Paint();
			p.setStyle(Paint.Style.FILL_AND_STROKE);
			p.setColor(Color.BLACK);
			p.setStrokeWidth(borderSize);		
		}
	
		return p;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if(borderLeft) {
			getPaint().setColor(0xFFc7ced1);
			canvas.drawLine(0, 0, 0, getMeasuredHeight(), getPaint());
		}
		if(borderRight) {
			getPaint().setColor(0xFFc7ced1);
			canvas.drawLine(getMeasuredWidth(), 0, getMeasuredWidth(), getMeasuredHeight(), getPaint());
			getPaint().setColor(0xFFe8eced);
			canvas.drawLine(getMeasuredWidth()-2, 0, getMeasuredWidth()-2, getMeasuredHeight(), getPaint());
		}
		if(borderTop) {
			getPaint().setColor(0xFFc7ced1);
			canvas.drawLine(0, 0, getMeasuredWidth(), 0, getPaint());
		}
		if(borderBottom) {
			getPaint().setColor(0xFFc7ced1);
			canvas.drawLine(0, getMeasuredHeight(), getMeasuredWidth(), getMeasuredHeight(), getPaint());
			getPaint().setColor(0xFFe8eced);
			canvas.drawLine(0, getMeasuredHeight()-2, getMeasuredWidth(), getMeasuredHeight()-2, getPaint());
			}
		
	}
	
	public int getBorderSize() {
		return borderSize;
	}
	
	public void setBorderSize(int borderSize) {
		this.borderSize = borderSize;
	}
		
}