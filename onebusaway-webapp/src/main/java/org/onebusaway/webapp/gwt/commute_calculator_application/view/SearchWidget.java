package org.onebusaway.webapp.gwt.commute_calculator_application.view;

import org.onebusaway.webapp.gwt.common.widgets.DivPanel;
import org.onebusaway.webapp.gwt.common.widgets.SpanWidget;
import org.onebusaway.webapp.gwt.commute_calculator_application.control.Control;
import org.onebusaway.webapp.gwt.commute_calculator_application.model.CommuteConstraints;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class SearchWidget extends FlowPanel {

  private TextBox _addressTextBox;

  private DivPanel _moreOptionsPanel;

  private DivPanel _optionsPanel;

  private TextBox _maxLengthTextBox;

  private ListBox _maxTransfersListBox;

  private ListBox _maxWalkDistance;

  private boolean _usingLocation = false;

  private Control _control;

  public void setControl(Control control) {
    _control = control;
  }

  public void initialize() {

    addStyleName("SearchWidget");

    FormPanel form = new FormPanel();
    add(form);

    FlowPanel panel = new FlowPanel();
    form.add(panel);

    DivPanel searchPanel = new DivPanel();
    searchPanel.addStyleName("SearchWidget-SearchPanel");
    panel.add(searchPanel);

    DivPanel addressPanel = new DivPanel();
    addressPanel.addStyleName("SearchWidget-AddressPanel");
    searchPanel.add(addressPanel);

    SpanWidget addressLabel = new SpanWidget("Where do you work:");
    addressPanel.add(addressLabel);

    _addressTextBox = new TextBox();
    _addressTextBox.setName("address");
    _addressTextBox.setVisibleLength(35);
    addressPanel.add(_addressTextBox);

    Button button = new Button("Go");
    addressPanel.add(button);

    button.addClickListener(new ClickListener() {
      public void onClick(Widget widget) {
        handleQuery();
      }
    });

    AddressTextBoxHandler handler = new AddressTextBoxHandler();
    _addressTextBox.addKeyboardListener(handler);
    _addressTextBox.addFocusListener(handler);

    _moreOptionsPanel = new DivPanel();
    _moreOptionsPanel.addStyleName("SearchWidget-OptionsPanel");
    panel.add(_moreOptionsPanel);

    Anchor moreOptionsButton = new Anchor("Show More Options");
    moreOptionsButton.addStyleName("SearchWidget-ShowOptionsButton");
    _moreOptionsPanel.add(moreOptionsButton);
    moreOptionsButton.addClickListener(new ExpansionClickHandler(true));

    _optionsPanel = new DivPanel();
    _optionsPanel.addStyleName("SearchWidget-OptionsPanel");
    panel.add(_optionsPanel);

    Grid optionsGrid = new Grid(3, 2);
    optionsGrid.addStyleName("SearchWidget-OptionsGrid");
    optionsGrid.getColumnFormatter().addStyleName(0,
        "SearchWidget-OptionsGrid-LabelsColumn");
    _optionsPanel.add(optionsGrid);

    SpanWidget maxLengthLabel = new SpanWidget("Trip Time:");
    optionsGrid.setWidget(0, 0, maxLengthLabel);

    _maxLengthTextBox = new TextBox();
    _maxLengthTextBox.setText("30");
    _maxLengthTextBox.addStyleName("SearchWidget-TripTimeTextBox");
    optionsGrid.setWidget(0, 1, _maxLengthTextBox);

    SpanWidget maxTransfersLabel = new SpanWidget("Transfers:");
    optionsGrid.setWidget(1, 0, maxTransfersLabel);

    _maxTransfersListBox = new ListBox();
    _maxTransfersListBox.addItem("Don't Care");
    _maxTransfersListBox.addItem("0");
    _maxTransfersListBox.addItem("1");
    _maxTransfersListBox.addItem("2");
    optionsGrid.setWidget(1, 1, _maxTransfersListBox);

    SpanWidget maxWalkLabel = new SpanWidget("Walk at most:");
    optionsGrid.setWidget(2, 0, maxWalkLabel);

    _maxWalkDistance = new ListBox();
    _maxWalkDistance.addItem("1/4 mile", "1320");
    _maxWalkDistance.addItem("1/2 mile", "2640");
    _maxWalkDistance.addItem("3/4 mile", "3960");
    _maxWalkDistance.addItem("1 mile", "5280");
    optionsGrid.setWidget(2, 1, _maxWalkDistance);

    DivPanel optionsPanelRowB = new DivPanel();
    _optionsPanel.add(optionsPanelRowB);

    Anchor lessOptionsButton = new Anchor("Hide Options");
    lessOptionsButton.addStyleName("SearchWidget-ShowOptionsButton");
    optionsPanelRowB.add(lessOptionsButton);
    lessOptionsButton.addClickListener(new ExpansionClickHandler(false));

    toggleExpansion(false);
  }

  private void toggleExpansion(boolean expand) {
    _moreOptionsPanel.setVisible(!expand);
    _optionsPanel.setVisible(expand);
  }

  private void handleQuery() {

    String query = _addressTextBox.getText();
    CommuteConstraints constraints = getConstraints();
    _control.performQuery(query, constraints);
  }

  private CommuteConstraints getConstraints() {

    CommuteConstraints constraints = new CommuteConstraints();

    constraints.setMinDepartureTimeOfDay(17 * 60 * 60);
    constraints.setMaxDepartureTimeOfDay(18 * 60 * 60);

    int maxTransfersIndex = _maxTransfersListBox.getSelectedIndex();
    switch (maxTransfersIndex) {
      case 0:
        constraints.setMaxTransfers(-1);
        break;
      case 1:
        constraints.setMaxTransfers(0);
        break;
      case 2:
        constraints.setMaxTransfers(1);
        break;
      case 3:
        constraints.setMaxTransfers(2);
        break;
    }
    constraints.setMaxTripDuration(Integer.parseInt(_maxLengthTextBox.getText()));

    int index = _maxWalkDistance.getSelectedIndex();
    if (index != -1)
      constraints.setMaxWalkingDistance(Integer.parseInt(_maxWalkDistance.getValue(index)));

    return constraints;
  }

  private class ExpansionClickHandler implements ClickListener {

    private boolean _expand;

    public ExpansionClickHandler(boolean expand) {
      _expand = expand;
    }

    public void onClick(Widget arg0) {
      toggleExpansion(_expand);
    }
  }

  private class AddressTextBoxHandler extends KeyboardListenerAdapter implements
      FocusListener {

    public void onKeyPress(Widget widget, char keyCode, int modifiers) {
      if (keyCode == KeyboardListener.KEY_ENTER && modifiers == 0) {
        handleQuery();
      } else {
        _usingLocation = false;
      }
    }

    public void onFocus(Widget widget) {
      if (_usingLocation)
        _addressTextBox.selectAll();
    }

    public void onLostFocus(Widget widget) {

    }
  }

}
