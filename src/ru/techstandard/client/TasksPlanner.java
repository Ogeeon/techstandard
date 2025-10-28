package ru.techstandard.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.techstandard.client.model.Task;
import ru.techstandard.client.model.Week;
import ru.techstandard.client.model.WeekProps;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.core.client.util.DateWrapper;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.Padding;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutData;
import com.sencha.gxt.widget.core.client.container.CenterLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer.HBoxLayoutAlign;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.CellClickEvent;
import com.sencha.gxt.widget.core.client.event.CellClickEvent.CellClickHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.grid.CellSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridViewConfig;
import com.sencha.gxt.widget.core.client.tips.QuickTip;

public class TasksPlanner extends ContentPanel implements HasValue<Date> {
	private final Images IMAGES = GWT.create(Images.class);
	HBoxLayoutContainer toolbarHLC;
	TextButton substactMonth;
	TextButton addMonth;
	Grid<Week> weekGrid;
	ContentPanel plannerPanel;
	Label monthLabel;
	int today;
	int thisMonth;
	int currMonth;
	int currYear;
	int prevMonthDays;
	int currMonthDays;
	ListStore<Week> plannerStore;
	Map<Integer, Integer> intensity;
	Map<Integer, String> descriptions;
	String[] monthNames = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
	List<Task> executorTasks;
	int greenThreshold = 2;
	int yellowThreshold = 4;
	
	private Date selectedDate = null;
	  
	public TasksPlanner() {
		super();
		plannerPanel = this;
		plannerPanel.setPixelSize(179, 182);
		plannerPanel.setHeaderVisible(false);
		plannerPanel.setBorders(false);
		
		today = (new DateWrapper()).getDate();
		thisMonth = (new DateWrapper()).getMonth();
		
		VerticalLayoutContainer plannerContainer = new VerticalLayoutContainer();
		this.setWidget(plannerContainer);
		
		toolbarHLC = new HBoxLayoutContainer();
		toolbarHLC.setHeight(23);
        toolbarHLC.setPadding(new Padding(0));
        toolbarHLC.setHBoxLayoutAlign(HBoxLayoutAlign.MIDDLE);

        BoxLayoutData flex = new BoxLayoutData(new Margins(0));
        flex.setFlex(0);
        substactMonth = new TextButton("");
        substactMonth.setIcon(IMAGES.left());
        substactMonth.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				shiftToPrevMonth();
			}
		});
        toolbarHLC.add(substactMonth, flex);

        BoxLayoutData flex2 = new BoxLayoutData(new Margins(0));
        flex2.setFlex(3);
        
        CenterLayoutContainer centerLayoutContainer = new CenterLayoutContainer();
        centerLayoutContainer.setBorders(false);
        monthLabel = new Label("month");
        centerLayoutContainer.setEnabled(false);
        centerLayoutContainer.add(monthLabel);
        toolbarHLC.add(centerLayoutContainer, flex2);
        
        addMonth = new TextButton("");
        addMonth.setIcon(IMAGES.right());
        addMonth.addSelectHandler(new SelectHandler() {
			@Override
			public void onSelect(SelectEvent event) {
				shiftToNextMonth();
			}
		});
        toolbarHLC.add(addMonth, flex);
        plannerContainer.add(toolbarHLC, new VerticalLayoutData(1, -1));
		
		WeekProps weekProps = GWT.create(WeekProps.class);
		plannerStore = new ListStore<Week>(new ModelKeyProvider<Week>() {
			@Override
			public String getKey(Week item) {
				return "" + item.getId();
			}
		});
		
		AbstractCell<Integer> cell = new AbstractCell<Integer>() {
			@Override
			public void render(Context context, Integer value, SafeHtmlBuilder sb) {
				String style = "style='color: " + (value < 0 ? "grey" : "black") + "'";
				Week week = weekGrid.getStore().get(context.getIndex());
				int dayNum = week.getDay(context.getColumn());
				String qt;
				if (!descriptions.containsKey(dayNum))
					qt = "";
				else
					qt = " qtip='" + descriptions.get(dayNum) + "' ";
				sb.appendHtmlConstant("<span " + style + qt + ">" + String.valueOf(Math.abs(value)) + "</span>");
			}
		};
		ColumnConfig<Week, Integer> monColumn = new ColumnConfig<Week, Integer>(weekProps.mon(), 1, "Пн");
		monColumn.setMenuDisabled(true);
		monColumn.setCell(cell);
		ColumnConfig<Week, Integer> tueColumn = new ColumnConfig<Week, Integer>(weekProps.tue(), 1, "Вт");
		tueColumn.setMenuDisabled(true);
		tueColumn.setCell(cell);
		ColumnConfig<Week, Integer> wedColumn = new ColumnConfig<Week, Integer>(weekProps.wed(), 1, "Ср");
		wedColumn.setMenuDisabled(true);
		wedColumn.setCell(cell);
		ColumnConfig<Week, Integer> thuColumn = new ColumnConfig<Week, Integer>(weekProps.thu(), 1, "Чт");
		thuColumn.setMenuDisabled(true);
		thuColumn.setCell(cell);
		ColumnConfig<Week, Integer> friColumn = new ColumnConfig<Week, Integer>(weekProps.fri(), 1, "Пт");
		friColumn.setMenuDisabled(true);
		friColumn.setCell(cell);
		ColumnConfig<Week, Integer> satColumn = new ColumnConfig<Week, Integer>(weekProps.sat(), 1, "Сб");
		satColumn.setMenuDisabled(true);
		satColumn.setCell(cell);
		ColumnConfig<Week, Integer> sunColumn = new ColumnConfig<Week, Integer>(weekProps.sun(), 1, "Вс");
		sunColumn.setMenuDisabled(true);
		sunColumn.setCell(cell);

		List<ColumnConfig<Week, ?>> l = new ArrayList<ColumnConfig<Week, ?>>();
		l.add(monColumn);
		l.add(tueColumn);
		l.add(wedColumn);
		l.add(thuColumn);
		l.add(friColumn);
		l.add(satColumn);
		l.add(sunColumn);
		ColumnModel<Week> cm = new ColumnModel<Week>(l);
		
		weekGrid = new Grid<Week>(plannerStore, cm) {};
		weekGrid.getView().setSortingEnabled(false);
		weekGrid.getView().setForceFit(true);
		weekGrid.getView().setStripeRows(false);
		weekGrid.getView().setColumnLines(true);
		weekGrid.setBorders(true);
		weekGrid.setColumnReordering(false);
		weekGrid.setColumnResize(false);
		weekGrid.getView().setTrackMouseOver(false);
		new QuickTip(weekGrid);
		
		weekGrid.setSelectionModel(new CellSelectionModel<Week>());
		
		weekGrid.getView().setViewConfig(new GridViewConfig<Week>() {
			@Override
			public String getColStyle(Week week, ValueProvider<? super Week, ?> valueProvider, int rowIndex, int colIndex) {
				String style = "";
				int dayNum = week.getDay(colIndex);
				if (dayNum == today && currMonth == thisMonth) 
					style += "currentday ";
				if (dayNum == (today+1) && currMonth == thisMonth) 
					style += "nextday ";
				if (intensity.containsKey(dayNum)) {
					int tasksCount = intensity.get(dayNum);
					if (tasksCount < greenThreshold)
						style += "green";
					else if (tasksCount < yellowThreshold)
						style += "yellow";
					else style += "red";
				}
				return style;
			}
			@Override
			public String getRowStyle(Week model, int rowIndex) { return null; }
		});
		
		weekGrid.addCellClickHandler(new CellClickHandler() {
			@Override
			public void onCellClick(CellClickEvent event) {
				int dayNum = plannerStore.get(event.getRowIndex()).getDay(event.getCellIndex());
				if (dayNum > 0) {
					selectedDate = (new DateWrapper(currYear, currMonth, dayNum)).asDate();
					ValueChangeEvent.fire((TasksPlanner) plannerPanel, selectedDate);
					return;
				}
				// Во второй половине таблицы отрицательные числа - это следующий месяц, в первой - предыдущий
				if (event.getRowIndex() > 2)
					shiftToNextMonth();
				else
					shiftToPrevMonth();
				List<Week> weeks = plannerStore.getAll();
				for (int w = 0; w < weeks.size(); w++) {
					for (int d = 0; d < 7; d++) {
						if (weeks.get(w).getDay(d) == Math.abs(dayNum)) {
							((CellSelectionModel<Week>) weekGrid.getSelectionModel()).selectCell(w, d);
							selectedDate = (new DateWrapper(currYear, currMonth, Math.abs(dayNum))).asDate();
							ValueChangeEvent.fire((TasksPlanner) plannerPanel, selectedDate);
						}
					}
				}
			}
		});
//		weekGrid.setPixelSize(185, 150);
		
		plannerContainer.add(weekGrid, new VerticalLayoutData(-1, -1));
		plannerContainer.forceLayout();
		
		intensity = new HashMap<Integer, Integer>();
		descriptions = new HashMap<Integer, String>();
	}
	
	public void setPeriod(int month, int year) {
		selectedDate = null;
		currMonth = month;
		currYear = year;
		redrawGrid();
	}
	
	private void redrawGrid() {
		monthLabel.setText(monthNames[currMonth]+" "+currYear);
		toolbarHLC.forceLayout();
		
		intensity.clear();
		descriptions.clear();
		
		DateWrapper dateWrapper = new DateWrapper(currYear, currMonth, 1);
		int shift = dateWrapper.getDayInWeek()==0?7:dateWrapper.getDayInWeek();
		currMonthDays = dateWrapper.getDaysInMonth();
		prevMonthDays = dateWrapper.addMonths(-1).getDaysInMonth();
		
		plannerStore.clear();
		List<Week> weeks = new ArrayList<Week>();
		int curr = -shift+1;
		// curr - это смещение в днях от 1го числа текущего месяца. поэтому curr == -1 указывает на последнюю дату предыщущего месяца		
		for (int weekN = 0; weekN < 6; weekN++) {
			Week w = new Week(weekN);
			for (int dayN = 0; dayN < 7; dayN++) {
				if (curr < 0)
					w.setDay(dayN, -(prevMonthDays + curr+1));
				else if (curr+1 > currMonthDays)
					w.setDay(dayN, -(curr+1 - currMonthDays));
				else
					w.setDay(dayN, curr+1);
				updateIntensity(curr);
				curr++;
			}
			weeks.add(w);
		}
		plannerStore.replaceAll(weeks);
		weekGrid.getView().refresh(false);
		plannerPanel.forceLayout();
	}
	
	private void shiftToPrevMonth() {
		currMonth--;
		if (currMonth < 0) {
			currMonth = 11;
			currYear--;
		}
		redrawGrid();		
	}
	
	private void shiftToNextMonth() {
		currMonth++;
		if (currMonth > 11) {
			currMonth = 0;
			currYear++;
		}
		redrawGrid();		
	}
	
	public Date getValue() {
		return selectedDate;
	}

	@Override
	public HandlerRegistration addValueChangeHandler( ValueChangeHandler<Date> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public void setValue(Date value) {
		((TasksPlanner) plannerPanel).setValue(value, false);
	}

	@Override
	public void setValue(Date value, boolean fireEvents) {
		DateWrapper wr = new DateWrapper(value);
		int yearNum = wr.getFullYear();
		int monthNum = wr.getMonth();
		setPeriod(monthNum, yearNum);
		int dayNum = wr.getDate();
		List<Week> weeks = plannerStore.getAll();
		for (int w = 0; w < weeks.size(); w++) {
			for (int d = 0; d < 7; d++) {
				if (weeks.get(w).getDay(d) == dayNum) {
//					((CellSelectionModel<Week>) weekGrid.getSelectionModel()).selectCell(w, d);
					selectedDate = (new DateWrapper(currYear, currMonth, dayNum)).asDate();
					if (fireEvents)
						ValueChangeEvent.fire((TasksPlanner) plannerPanel, selectedDate);
					return;
				}
			}
		}	
	}
	
	public void setExecutorTasks(List<Task> tasks) {
		executorTasks = tasks;
		redrawGrid();
	}
	
	/**
	 * Обновляет данные о занятости исполнителя
	 * @param shift смещение в днях от 1го числа текущего месяца
	 */
	private void updateIntensity(int shift) {
		if (executorTasks == null || executorTasks.isEmpty())
			return;
		DateWrapper wr = new DateWrapper(currYear, currMonth, 1).addDays(shift);
		long currMS = wr.asDate().getTime();
		long startMS = 0;
		long dueMS = 0;
		int key = 0;
		for (Task t: executorTasks) {
			if (t.getStartDate() == null || t.getDueDate() == null)
				continue;
			startMS = t.getStartDate().getTime();
			dueMS = t.getDueDate().getTime();
			if (startMS <= currMS && currMS <= dueMS) {
				// Ключом в intensity является номер дня в месяце (с минусом, если это предыдущий или следующий месяц)
				if (shift < 0)
					key = -(prevMonthDays + shift+1);
				else if (shift+1 > currMonthDays)
					key = -(shift+1 - currMonthDays);
				else
					key = shift+1;
				int prevInt = intensity.containsKey(key) ? intensity.get(key) : 0;
				intensity.put(key, prevInt+1);
				String prevDescr = descriptions.containsKey(key) ? descriptions.get(key) : "";
				descriptions.put(key, (prevDescr.isEmpty() ? "" : prevDescr+"<br>") + "&#8226; " + t.getDescription());
			}
		}
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		weekGrid.setEnabled(enabled);
		substactMonth.setEnabled(enabled);
		addMonth.setEnabled(enabled);
	}
}
