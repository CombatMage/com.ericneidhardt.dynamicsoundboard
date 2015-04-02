import android.app.Activity;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neidhardt.dynamicsoundboard.BaseActivity;
import org.neidhardt.dynamicsoundboard.R;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Created by eric.neidhardt on 02.04.2015.
 */

@RunWith(CustomRobolectricRunner.class)
public class RobolectricTest {
	@Test
	public void testIt() {
		Activity activity = Robolectric.setupActivity(BaseActivity.class);

		FrameLayout results = (FrameLayout) activity.findViewById(R.id.main_frame);
		int childCount = results.getChildCount();

		// failing test gives much better feedback
		// to show that all works correctly ;)
		assertThat(childCount, equalTo(1));
	}
}

