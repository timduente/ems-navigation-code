package de.duente.navigation.study;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


/**Stellt die Datenpunkte die vom Optitracksystem gesendet werden in einer 2D Ebene dar.
 * 
 * @author Tim Dünte
 *
 */
public class TrackingView extends View {
	private Paint paint;
	private Bitmap img;
	
	private boolean signalActive = false;

	/**
	 * @return the signalActive
	 */
	public boolean isSignalActive() {
		return signalActive;
	}

	/**
	 * @param signalActive the signalActive to set
	 */
	public void setSignalActive(boolean signalActive) {
		this.signalActive = signalActive;
	}

	private boolean drawMe = false;

	public TrackingView(Context context) {
		super(context);
		initPaint();
		// TODO Auto-generated constructor stub
	}

	public TrackingView(Context context, AttributeSet attrSet) {
		super(context, attrSet);
		initPaint();
		// TODO Auto-generated constructor stub
	}

	private void initPaint() {
		paint = new Paint();
		paint.setAntiAlias(true);	
	}

	/**Uebergabe der Koordinaten, die dargestellt werden sollen.
	 * x und z sind im Moment die Zeichenachsen.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setCoordinateToDraw(float x, float y, float z) {
		int xPic = 0;
		int yPic = 0;
		if (x != 0.0f && y != 0.0f && z != 0.0f) {
			xPic = (int) ((x * 40.0f) + getWidth() / 4);
			yPic = (int) ((z * 40.0f) + getHeight() / 10);
			
			//Damit die Grenzen des Images nicht überschritten werden. 
			xPic = Math.min(Math.max(0, xPic), getWidth() - 1);
			yPic = Math.min(Math.max(0, yPic), getHeight() - 1);
			
			if(signalActive){
				img.setPixel(xPic, yPic, Color.RED);	
			}else{
				img.setPixel(xPic, yPic, Color.BLACK);
			}
					
			drawMe = true;
			this.invalidate();
		}
	}
	/**Initialisiert die Zeichenfläche. Löscht das Bild. 
	 * 
	 */
	public void clear(){
		img = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		img.eraseColor(Color.WHITE);
		this.invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawText("TrackingView", 10, 10, paint);
		
		if (drawMe) {
			canvas.drawBitmap(img, 0.0f, 0.0f, paint);			
			drawMe = false;
			paint.setColor(Color.WHITE);
		}
	}
}
