package games.stendhal.client.gui.stats;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;

public class MasteryPanel extends JPanel implements PropertyChangeListener {
	private static final long serialVersionUID = 1L;

	private final JLabel headline;
	private final JProgressBar progress;
	private final JLabel detail;

	public MasteryPanel() {
		super();
		setLayout(new SBoxLayout(SBoxLayout.VERTICAL, 1));
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		headline = new JLabel();
		headline.setForeground(new Color(0xE2B84A));
		add(headline, SLayout.EXPAND_X);

		progress = new JProgressBar();
		progress.setForeground(new Color(0xE2B84A));
		progress.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
		add(progress, SLayout.EXPAND_X);

		detail = new JLabel();
		add(detail, SLayout.EXPAND_X);

		MasteryProgressController.get().addProgressListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt == null || !(evt.getNewValue() instanceof MasteryProgress)) {
			return;
		}
		MasteryProgress progressState = (MasteryProgress) evt.getNewValue();
		MasteryPresentation presentation = MasteryPresentation.from(progressState);
		headline.setText(presentation.headline);
		detail.setText(presentation.detail);
		progress.setMaximum(presentation.progressMax);
		progress.setValue(presentation.progressValue);
		progress.setStringPainted(presentation.unlocked);
		if (presentation.unlocked) {
			progress.setString(progressState.getCurrentExp() + " / " + (progressState.getCurrentExp() + Math.max(0L, progressState.getExpToNext())));
		} else {
			progress.setString("");
		}
	}
}
