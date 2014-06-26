package org.tagsys.tagsee.gui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.TimeoutException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import org.llrp.ltk.exceptions.InvalidLLRPMessageException;
import org.tagsys.tagsee.ImpinJReader;
import org.tagsys.tagsee.Observation;
import org.tagsys.tagsee.ReadListener;

public class MainController extends AnchorPane implements Initializable,
		ReadListener {

	Map<String, List<Observation>> data = new HashMap<String, List<Observation>>();

	ImpinJReader reader = new ImpinJReader();

	@FXML
	Hyperlink moreInfoLink;
	@FXML
	Label errorMessage;
	@FXML
	TextField ipText;
	@FXML
	TextField timeText;
	@FXML
	TextField directoryText;
	@FXML
	ListView<String> freqList;
	@FXML
	ComboBox epcCombo;
	@FXML
	TableView dataTable;
	@FXML
	TableView resultTable;
	@FXML
	Button collectButton;
	@FXML
	Button annalyseButton;

	@Override
	public void read(Observation obs) {

		System.out.println(obs);

		if (!data.containsKey(obs.getEpc())) {
			data.put(obs.getEpc(), new ArrayList<Observation>());
		}

		data.get(obs.getEpc()).add(obs);

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		String[] strings = new String[ImpinJReader.FREQUENCIES.length];
		for (int i = 0; i < ImpinJReader.FREQUENCIES.length; i++)
			strings[i] = "" + ImpinJReader.FREQUENCIES[i];

		this.freqList.setItems(FXCollections.observableArrayList(strings));

		this.epcCombo.getItems().clear();
		this.ipText.setText("192.168.1.212");
		this.timeText.setText("10000");
		this.reader.setReadListener(this);

		// table
		// ObservableList<TableColumn> columns = dataTable.getColumns();
		// columns.get(0).setCellValueFactory(
		// new PropertyValueFactory("channelIndex"));
		// columns.get(1).setCellValueFactory(new
		// PropertyValueFactory("doppler"));
		// columns.get(2).setCellValueFactory(
		// new PropertyValueFactory("wavePhase"));
		// columns.get(3).setCellValueFactory(new PropertyValueFactory("rssi"));
		// columns.get(4).setCellValueFactory(
		// new PropertyValueFactory("timeStamp"));
		//
		// columns = resultTable.getColumns();
		// columns.get(0).setCellValueFactory(
		// new PropertyValueFactory("channelIndex"));
		// columns.get(1).setCellValueFactory(new
		// PropertyValueFactory("phase"));
		// columns.get(2).setCellValueFactory(new
		// PropertyValueFactory("count"));
	}

	@FXML
	protected void processItemChanged(ActionEvent event) {
		String epc = (String) this.epcCombo.getSelectionModel()
				.getSelectedItem();

		// List<TagData> dataList = dataMap.get(epc);
		// TableTagData[] tableData = new TableTagData[dataList.size()];
		// int i = 0;
		// for (TagData d : dataList) {
		// tableData[i] = new TableTagData(d);
		// i++;
		// }
		//
		// this.dataTable.getItems().clear();
		// this.dataTable.setItems(FXCollections.observableArrayList(tableData));
	}

	@FXML
	protected void processCollecteData(ActionEvent event) {

		new Thread() {

			public void run() {

				try {
					int time = Integer.parseInt(timeText.getText());
					data.clear();
					reader.setIp(ipText.getText());
					reader.connect();
//					reader.deleteAllRoSpec();
					reader.enableImpinjExtensions();
					reader.factoryDefault();
					reader.sendReaderConfiguration("specs/SET_READER_CONFIG.xml");
					int roSpecId = reader.sendRoSpec("specs/ADD_ROSPEC.xml");
					reader.enableRoSpec(roSpecId);
					reader.startRoSpec(roSpecId);

					try {
						Thread.sleep(time);
					} catch (InterruptedException ex) {

					}

					reader.stopRoSpec(roSpecId);

					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							epcCombo.getItems().clear();
							dataTable.getItems().clear();
							resultTable.getItems().clear();

						}

					});
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						reader.disconnect();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}

		}.start();

	}

	@FXML
	protected void processAnnalyse(ActionEvent event) {
		//System.out.println((new java.util.Date()).getTime());
		System.out.print(System.currentTimeMillis()+"-");
	}

	@FXML
	protected void processMoreInfo(ActionEvent event) {
		this.errorMessage.setText("No more infomation for PANDA LOCALIZATION");
	}

	@FXML
	protected void processExportData(ActionEvent event) {

		if (data == null || data.size() == 0) {
			errorMessage.setText("No data collected.");
			return;
		}

		try {
			String dir = this.directoryText.getText();

			for (String epc : data.keySet()) {
				File file = new File(dir + epc + "-"
						+ System.currentTimeMillis() + ".csv");
				PrintWriter print = new PrintWriter(new BufferedWriter(
						new FileWriter(file)));

				List<Observation> dataList = data.get(epc);
				print.println(Observation.propertyHeads());
				for (Observation obs : dataList) {
					print.println(obs);
				}
				print.flush();
				print.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}