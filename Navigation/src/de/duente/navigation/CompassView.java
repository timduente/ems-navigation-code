package de.duente.navigation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.View;

public class CompassView extends View {
	float northX = 0, northY = 0;
	// float correctedNorthX = 0, correctedNorthY = 0;
	float directionX = 0, directionY = 0;
	float[] gravity = new float[3];
	double angle = 0.0;
	public final static double CORRECTION_FACTOR_MAGNETICFIELD_GER_HANNOVER = 2.0 * Math.PI / 180;

	double rotation = 0.0;

	float cDirectionX = 0;
	float cDirectionY = 0;

	public void setCDirection(float x, float y) {
		float norm = (float) Math.sqrt(x * x + y * y);
		cDirectionX = x / norm * radius;
		cDirectionY = y / norm * radius;
	}

	public CompassView(Context context) {
		super(context);
		initPaint();
		// TODO Auto-generated constructor stub
	}

	public CompassView(Context context, AttributeSet set) {
		super(context, set);
		initPaint();
		// TODO Auto-generated constructor stub
	}

	private void initPaint() {
		paint = new Paint();
		paint.setAntiAlias(true);
	}

	Paint paint;
	float radius;

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		radius = ((w < h) ? w : h) / 2.0f - 10.0f;
	}

	public void setNorth(float x1, float y1) {
		// System.out.println("Vorher x: " + x1+ " y: "+ y1 + " Rot: " +
		// rotation);

		float x = (float) (Math.cos(rotation) * x1 - Math.sin(rotation) * y1);
		float y = (float) (Math.sin(rotation) * x1 + Math.cos(rotation) * y1);

		directionX = (float) (Math.cos(angle) * x - Math.sin(angle) * y);
		directionY = (float) (Math.sin(angle) * x + Math.cos(angle) * y);
		float norm2 = (float) Math.sqrt(directionX * directionX + directionY
				* directionY);
		directionX = directionX / norm2 * radius;
		directionY = directionY / norm2 * radius;

		/*
		 * float correctedX = (float)(Math.cos(rotation-
		 * CORRECTION_FACTOR_MAGNETICFIELD_GER_HANNOVER) * x1 -
		 * Math.sin(rotation- CORRECTION_FACTOR_MAGNETICFIELD_GER_HANNOVER) *
		 * y1); float correctedY = (float)(Math.sin(rotation-
		 * CORRECTION_FACTOR_MAGNETICFIELD_GER_HANNOVER) * x1 +
		 * Math.cos(rotation- CORRECTION_FACTOR_MAGNETICFIELD_GER_HANNOVER) *
		 * y1); float norm3 = (float) Math.sqrt(correctedX * correctedX +
		 * correctedY * correctedY); correctedNorthX = correctedX / norm3 *
		 * radius; correctedNorthY = correctedY / norm3 * radius;
		 */

		// System.out.println("Rotiert x: " + x+ " y: "+ y);
		float norm = (float) Math.sqrt(x * x + y * y);
		northX = x / norm * radius;
		northY = y / norm * radius;
		// System.out.println("Norden: " + northX + " : " + northY);
	}

	public void setGravity(float[] gravity) {
		for (int i = 0; i < this.gravity.length; i++) {
			this.gravity[i] = gravity[i] * 3.0f;
		}
	}

	public void setRotation(int rotation) {
		if (rotation == Surface.ROTATION_0) {
			this.rotation = 0.0;
		} else if (rotation == Surface.ROTATION_90) {
			this.rotation = 0.5 * Math.PI;
		} else if (rotation == Surface.ROTATION_270) {
			this.rotation = 1.5 * Math.PI;
		}

		this.rotation = rotation + CORRECTION_FACTOR_MAGNETICFIELD_GER_HANNOVER;
	}

	public void setDirectionAngle(double angle) {
		this.angle = -Math.toRadians(angle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawText("Compass", 10, 10, paint);
		float mX = getWidth() / 2.0f;
		float mY = getHeight() / 2.0f;
		canvas.drawColor(Color.WHITE);

		// Schwerkraftbalken malen
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.BLUE);
		canvas.drawRect(0, -gravity[0] + 30.0f, 30, 30.0f, paint);
		paint.setColor(Color.GREEN);
		canvas.drawRect(30, -gravity[1] + 30.0f, 60, 30.0f, paint);
		paint.setColor(Color.RED);
		canvas.drawRect(60, -gravity[2] + 30.0f, 90, 30.0f, paint);

		paint.setStyle(Paint.Style.STROKE);

		// Schwerkraftanzeige zeichnen
		paint.setColor(Color.BLACK);
		canvas.drawText("<- Schwerkraft", 100, 30, paint);
		canvas.drawRect(0, 0, 90, 60, paint);
		canvas.drawLine(0, 30, 90, 30, paint);
		canvas.drawLine(30, 0, 30, 60, paint);
		canvas.drawLine(60, 0, 60, 60, paint);
		canvas.drawText("X", 5, 80, paint);
		canvas.drawText("Y", 35, 80, paint);
		canvas.drawText("Z", 65, 80, paint);

		// Compass zeichnen
		canvas.drawCircle(mX, mY, radius, paint);
		paint.setTextSize(20);
		canvas.drawText("N", northX + mX, -northY + mY, paint);
		canvas.drawText("S", -northX + mX, northY + mY, paint);
		paint.setColor(Color.RED);
		canvas.drawLine(mX, mY, northX + mX, -northY + mY, paint);
		paint.setColor(Color.GREEN);
		canvas.drawLine(mX, mY, -northX + mX, northY + mY, paint);
		paint.setColor(Color.BLUE);
		canvas.drawLine(mX, mY, directionX + mX, -directionY + mY, paint);
		
		 paint.setColor(Color.MAGENTA); canvas.drawLine(mX, mY,
		 cDirectionX + mX, -cDirectionY + mY, paint);
		 
		 if(cDirectionX>= 0){
			 canvas.drawText("nach rechts drehen",  mX, radius +20  + mY, paint);
		 }else{
			 canvas.drawText("nach links drehen",  mX, radius +20  + mY, paint);
		 }
		 
	}

}
