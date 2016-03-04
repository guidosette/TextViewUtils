import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by guido on 26/02/16.
 */
public class TextViewUtils extends AppCompatTextView {
	private final static String TAG = TextViewFitting.class.getSimpleName();

	private boolean mIsFitting = true;
	private boolean mAlreadyCheck = false;

	protected TextViewUtilsInterface mCallback;

	public interface TextViewUtilsInterface {
		void onGetEffectiveLineCount(int effectiveLineCount);
	}
	
	public void setOnViewReady(TextViewUtilsInterface callback) {
		mCallback = callback;
	}

	public TextViewUtils(Context context) {
		super(context);
	}

	public TextViewUtils(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TextViewUtils(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		checkLines();
	}

	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
		super.onTextChanged(text, start, lengthBefore, lengthAfter);

		//todo
//		if (mAlreadyCheck) {
//			checkLines();
//		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		checkLines();
	}
	
	private void checkLines() {
		final int lineCount = this.getLayout().getLineCount();
		final int lineEffectiveCount = this.getEffectiveLineCountTextView();
		if (lineEffectiveCount < lineCount && lineEffectiveCount != 0) {
			mAlreadyCheck = true;
			Log.i(TAG, "lineCount " + lineCount);
			Log.i(TAG, "lineEffectiveCount " + lineEffectiveCount);
			mIsFitting = false;
			this.setMaxLines(lineEffectiveCount);
		}
		if (mCallback != null && lineEffectiveCount != 0 && !mAlreadyCheck) {
			mAlreadyCheck = true;
			mCallback.onGetEffectiveLineCount(lineEffectiveCount);
		}
	}

	/**
	 * @return number effective line count of textView in his view. Because maybe is truncated
	 */
	public int getEffectiveLineCountTextView() {
		final String text = this.getText().toString();
		final int maxWidth = this.getWidth();
		final float textSize = this.getTextSize();
		final Typeface typeface = this.getTypeface();
		final int maxHeight = this.getHeight();
		final int lineHeigth = this.getLineHeight();

		final TextPaint paint = new TextPaint(Paint.LINEAR_TEXT_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		paint.setTextSize(textSize);
		paint.setTypeface(typeface);

		int lineCount = 0;
		int currentIndex = 0;
		int length = text.length();
		while (currentIndex < length - 1) {
			int nCharInLine = paint.breakText(text, currentIndex, length, true, maxWidth, null);
			int endIndex = nCharInLine;
			final String subText = text.substring(currentIndex, currentIndex + nCharInLine);
			if (subText.length() == 0) {
				return 0;
			}
			final String lastChar = String.valueOf(subText.charAt((subText.length() - 1)));
			int lastIndex = currentIndex + (subText.length() - 1);
			if (subText.contains("\n")) {
				int subIndex = subText.indexOf("\n");
				endIndex = subIndex + 1;
			} else if (lastChar.equalsIgnoreCase(" ")) {
				endIndex = nCharInLine;
			} else if (lastIndex < (length - 1)) { // if not last character
				int subIndex = subText.lastIndexOf(" ");
				endIndex = subIndex + 1;
			}
			currentIndex += endIndex;
			lineCount++;
			int currentHeigth = (int) Math.floor(lineCount * lineHeigth);
			if (currentHeigth >= maxHeight) {
				return lineCount - 1;
			}
		}
		return lineCount;
	}

	public boolean isFitting() {
		return mIsFitting;
	}
}
