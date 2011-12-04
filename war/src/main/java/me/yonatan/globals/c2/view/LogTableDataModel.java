package me.yonatan.globals.c2.view;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.Setter;
import me.yonatan.globals.c2.action.DbManager;
import me.yonatan.globals.c2.action.DbManager.Records;
import me.yonatan.globals.c2.entity.LogRecord;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

@SuppressWarnings("serial")
public class LogTableDataModel extends LazyDataModel<LogRecord> {

	@Inject
	private DbManager dbManager;

	@Setter
	private String handler;

	@Override
	public List<LogRecord> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
		System.out.println(filters);
		// setRowCount(dbManager.getLogCountInt(handler));
		Records records;

		// List<LogRecord> records;
		if (filters.containsKey("ip")) {
			records = dbManager.getRecords(handler, first, pageSize, filters.get("ip"));
		} else {
			records = dbManager.getRecords(handler, first, pageSize);
		}
		setRowCount(records.getTotalResults());
		System.out.println("total results " + records.getTotalResults());
		return records.getRecords();
	}

	// @Override
	// public LogRecord getRowData(String rowKey) {
	// List<LogRecord> records = dbManager.getRecords(handler);
	// for (LogRecord car : records) {
	// if (rowKey.equals(String.valueOf(car.getId())))
	// return car;
	// }
	//
	// return null;
	// }
	//
	// @Override
	// public Object getRowKey(LogRecord car) {
	// System.out.println("getRowKey " + car.getId());
	// return car.getId();
	// }
}
