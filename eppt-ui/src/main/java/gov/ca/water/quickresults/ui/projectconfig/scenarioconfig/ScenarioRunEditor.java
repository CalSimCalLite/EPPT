/*
 * Enhanced Post Processing Tool (EPPT) Copyright (c) 2019.
 *
 * EPPT is copyrighted by the State of California, Department of Water Resources. It is licensed
 * under the GNU General Public License, version 2. This means it can be
 * copied, distributed, and modified freely, but you may not restrict others
 * in their ability to copy, distribute, and modify it. See the license below
 * for more details.
 *
 * GNU General Public License
 */

package gov.ca.water.quickresults.ui.projectconfig.scenarioconfig;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.*;

import gov.ca.water.calgui.project.EpptScenarioRun;

/**
 * Company: Resource Management Associates
 *
 * @author <a href="mailto:adam@rmanet.com">Adam Korynta</a>
 * @since 04-08-2019
 */
public class ScenarioRunEditor extends JDialog implements LoadingDss
{
	private final ScenarioEditorPanel _scenarioEditorPanel;
	private final JProgressBar _progressBar = new JProgressBar();
	private boolean _canceled = true;

	public ScenarioRunEditor(Frame frame)
	{
		super(frame, "New Scenario Run", true);
		_progressBar.setVisible(false);
		_scenarioEditorPanel = new ScenarioEditorPanel(this);
		setPreferredSize(new Dimension(650, 500));
		setMinimumSize(new Dimension(650, 500));
		initComponents();
		pack();
		setLocationRelativeTo(frame);
	}

	public void fillPanel(EpptScenarioRun scenarioRun)
	{
		setTitle("Edit Scenario Run: " + scenarioRun.getName());
		_scenarioEditorPanel.fillPanel(scenarioRun);
	}

	private void initComponents()
	{
		setLayout(new BorderLayout(5, 5));
		add(_scenarioEditorPanel.$$$getRootComponent$$$(), BorderLayout.CENTER);
		buildOkCancelButtons();
	}

	private void buildOkCancelButtons()
	{
		JPanel jPanel = new JPanel();
		jPanel.setLayout(new BorderLayout());
		jPanel.add(_progressBar, BorderLayout.NORTH);
		JButton okButton = new JButton("OK");
		okButton.setDefaultCapable(true);
		getRootPane().setDefaultButton(okButton);
		JButton cancelButton = new JButton("Cancel");
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(okButton);
		okButton.setPreferredSize(cancelButton.getPreferredSize());
		buttonPanel.add(cancelButton);
		jPanel.add(buttonPanel, BorderLayout.CENTER);
		add(jPanel, BorderLayout.SOUTH);
		okButton.addActionListener(this::okPerformed);
		cancelButton.addActionListener(this::cancelPerformed);
	}

	@Override
	public void loadingStart(String text)
	{
		_progressBar.setVisible(true);
		_progressBar.setIndeterminate(true);
		_progressBar.setToolTipText(text);
	}

	@Override
	public void loadingFinished()
	{
		_progressBar.setVisible(false);
		_progressBar.setIndeterminate(false);
	}

	public void okPerformed(ActionEvent e)
	{
		_canceled = false;
		if(_scenarioEditorPanel.validateRun())
		{
			setVisible(false);
			dispose();
		}
		else
		{
			JOptionPane.showMessageDialog(this, "Scenario Run must have a name", "Error", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void cancelPerformed(ActionEvent e)
	{
		setVisible(false);
		dispose();
	}

	@Override
	public void dispose()
	{
		_progressBar.setIndeterminate(false);
		_progressBar.setVisible(false);
		_scenarioEditorPanel.shutdown();
		super.dispose();
	}

	/**
	 * @return null if canceled, builds a new Scenario Run otherwise
	 */
	public EpptScenarioRun createRun()
	{
		EpptScenarioRun retval = null;
		if(!_canceled && _scenarioEditorPanel.validateRun())
		{
			retval = _scenarioEditorPanel.createRun();
		}
		return retval;
	}
}
