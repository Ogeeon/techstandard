package ru.techstandard.client;

import ru.techstandard.client.model.AccessGroup;
import ru.techstandard.client.model.Constants;
import ru.techstandard.client.model.DictionaryRecord;
import ru.techstandard.client.model.DictionaryRecordProps;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

public class JournalsPanel extends VBoxLayoutContainer {

	ComboBox<DictionaryRecord> journalSelector;
	CardLayoutContainer cardLayout;
	CardLayoutContainer buttonsCardLayout;
	
	Widget actsJournal;
	Widget contractsJournal;
	Widget guidesJournal;
	Widget requestsJournal;
	
	AccessGroup group;
	int loggedUserId;
	
	public JournalsPanel(int userId, AccessGroup accGrp) {
		super();
		this.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		loggedUserId = userId;
		group = accGrp;
		
		ToolBar topToolBar = new ToolBar();
		BoxLayoutData flex = new BoxLayoutData(new Margins(0));
        flex.setFlex(0);
        this.add(topToolBar, flex);
        
        DictionaryRecordProps props = GWT.create(DictionaryRecordProps.class);
	    final ListStore<DictionaryRecord> store = new ListStore<DictionaryRecord>(props.key());
	    if (group.isAllowed(Constants.ACCESS_READ, "acts")) {
	    	DictionaryRecord drClients = new DictionaryRecord(Constants.JOURN_ACTS, "Экспертиз, актов");
	    	store.add(drClients);
	    }
	    if (group.isAllowed(Constants.ACCESS_READ, "contracts")) {
	    	DictionaryRecord drWorkSubjs = new DictionaryRecord(Constants.JOURN_CONTRACTS, "Договоров");
	    	store.add(drWorkSubjs);
	    }
	    if (group.isAllowed(Constants.ACCESS_READ, "guides")) {
	    	DictionaryRecord drObjTypes = new DictionaryRecord(Constants.JOURN_GUIDES, "Руководств, паспортов");
	    	store.add(drObjTypes);
	    }
	    if (group.isAllowed(Constants.ACCESS_READ, "requests")) {
	    	DictionaryRecord drAttachTypes = new DictionaryRecord(Constants.JOURN_REQUESTS, "Прочих задач"); 
	    	store.add(drAttachTypes);
	    }
	    
        journalSelector = new ComboBox<DictionaryRecord>(store, props.nameLabel());
	    journalSelector.setEditable(false);
	    journalSelector.setAllowBlank(false);
	    journalSelector.setForceSelection(true);
	    journalSelector.setTriggerAction(TriggerAction.ALL);
	    journalSelector.setValue(store.get(0));
	    journalSelector.addSelectionHandler(new SelectionHandler<DictionaryRecord>() {
			@Override
			public void onSelection(SelectionEvent<DictionaryRecord> event) {
				DictionaryRecord item = event.getSelectedItem();
				
				switch (item.getId()) {
					case Constants.JOURN_ACTS: { if (group.isAllowed(Constants.ACCESS_READ, "acts")) {cardLayout.setActiveWidget(actsJournal); buttonsCardLayout.setActiveWidget(buttonsCardLayout.getWidget(0));} break;}
					case Constants.JOURN_CONTRACTS: { if (group.isAllowed(Constants.ACCESS_READ, "contracts")) {cardLayout.setActiveWidget(contractsJournal); buttonsCardLayout.setActiveWidget(buttonsCardLayout.getWidget(1));} break;}
					case Constants.JOURN_GUIDES: { if (group.isAllowed(Constants.ACCESS_READ, "guides")) {cardLayout.setActiveWidget(guidesJournal); buttonsCardLayout.setActiveWidget(buttonsCardLayout.getWidget(2));} break;}
					case Constants.JOURN_REQUESTS: { if (group.isAllowed(Constants.ACCESS_READ, "requests")) {cardLayout.setActiveWidget(requestsJournal); buttonsCardLayout.setActiveWidget(buttonsCardLayout.getWidget(3));} break;}
					default: {cardLayout.setActiveWidget(actsJournal); buttonsCardLayout.setActiveWidget(buttonsCardLayout.getWidget(0));}
				}
				cardLayout.forceLayout();
			}
		});
	    
	    
	    FieldLabel selectorLbl = new FieldLabel(journalSelector, "Журнал");
	    selectorLbl.setWidth(220);
	    selectorLbl.setLabelWidth(50);
	    topToolBar.add(selectorLbl);
	    
	    BoxLayoutData flex2 = new BoxLayoutData(new Margins(0));
        flex2.setFlex(3);
        
        cardLayout = new CardLayoutContainer();
        buttonsCardLayout = new CardLayoutContainer();
        
        if (group.isAllowed(Constants.ACCESS_READ, "acts")) {
        	actsJournal = new ActsJournalPanel(group);
        	cardLayout.add(actsJournal);
            buttonsCardLayout.add(((ActsJournalPanel) actsJournal).getButtonsContainer());
        }
        if (group.isAllowed(Constants.ACCESS_READ, "contracts")) {
        	contractsJournal = new ContractsJournalPanel(loggedUserId, group);
        	cardLayout.add(contractsJournal);
            buttonsCardLayout.add(((ContractsJournalPanel) contractsJournal).getButtonsContainer());
        }
        if (group.isAllowed(Constants.ACCESS_READ, "guides")) {
        	guidesJournal = new GuidesJournalPanel(loggedUserId, group);
        	cardLayout.add(guidesJournal);
            buttonsCardLayout.add(((GuidesJournalPanel) guidesJournal).getButtonsContainer());
        }
        if (group.isAllowed(Constants.ACCESS_READ, "requests")) {
        	requestsJournal = new RequestsJournalPanel(loggedUserId, group);
        	cardLayout.add(requestsJournal);
            buttonsCardLayout.add(((RequestsJournalPanel) requestsJournal).getButtonsContainer());
        }
        
        cardLayout.setActiveWidget(cardLayout.getWidget(0));
        buttonsCardLayout.setActiveWidget(buttonsCardLayout.getWidget(0));
        
        topToolBar.add(buttonsCardLayout);
        
        
        this.add(cardLayout, flex2);
	}
}
