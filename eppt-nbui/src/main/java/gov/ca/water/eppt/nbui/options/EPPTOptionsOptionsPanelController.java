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
package gov.ca.water.eppt.nbui.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.*;

import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.TopLevelRegistration(
		categoryName = "#OptionsCategory_Name_EPPTOptions",
		iconBase = "gov/ca/water/eppt/nbui/options/frame32.gif",
		keywords = "#OptionsCategory_Keywords_EPPTOptions",
		keywordsCategory = "EPPTOptions"
)
@org.openide.util.NbBundle.Messages(
		{
				"OptionsCategory_Name_EPPTOptions=EPPT Options", "OptionsCategory_Keywords_EPPTOptions=EpptOptions"
		})
public final class EPPTOptionsOptionsPanelController extends OptionsPanelController
{

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private EPPTOptionsPanel panel;
	private boolean changed;

	public void update()
	{
		getPanel().load();
		changed = false;
	}

	public void applyChanges()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				getPanel().store();
				changed = false;
			}
		});
	}

	public void cancel()
	{
		// need not do anything special, if no changes have been persisted yet
	}

	public boolean isValid()
	{
		return getPanel().valid();
	}

	public boolean isChanged()
	{
		return changed;
	}

	public HelpCtx getHelpCtx()
	{
		return null; // new HelpCtx("...ID") if you have a help set
	}

	public JComponent getComponent(Lookup masterLookup)
	{
		return getPanel();
	}

	public void addPropertyChangeListener(PropertyChangeListener l)
	{
		pcs.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l)
	{
		pcs.removePropertyChangeListener(l);
	}

	private EPPTOptionsPanel getPanel()
	{
		if(panel == null)
		{
			panel = new EPPTOptionsPanel(this);
		}
		return panel;
	}

	void changed()
	{
		if(!changed)
		{
			changed = true;
			pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
		}
		pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
	}

}
