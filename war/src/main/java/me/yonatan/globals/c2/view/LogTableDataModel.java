package me.yonatan.globals.c2.view;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import me.yonatan.globals.c2.action.DbManager;
import me.yonatan.globals.c2.action.DbManager.Records;
import me.yonatan.globals.c2.entity.LogRecord;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

@SuppressWarnings("serial")
public class LogTableDataModel extends LazyDataModel<LogRecord> {

	@Inject
	private DbManager dbManager;

	@Setter
	private String handler;

	@Getter
	@Setter
	private Date fromDateFilter;

	@Getter
	@Setter
	private Date toDateFilter;

	@Getter
	@Setter
	private String ipFilter;

	@Override
	public List<LogRecord> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
		System.out.println(filters);
		Records records;
		System.out.println("from date " + fromDateFilter + " to " + toDateFilter + " IP " + ipFilter);

		if ((fromDateFilter != null || toDateFilter != null) && StringUtils.isNotBlank(ipFilter)) {
			records = dbManager.getRecords(handler, first, pageSize, ipFilter, (fromDateFilter != null ? fromDateFilter.getTime() : 0),
					(toDateFilter != null ? toDateFilter.getTime() : Long.MAX_VALUE));
		} else if ((fromDateFilter == null && toDateFilter == null) && StringUtils.isNotBlank(ipFilter)) {
			records = dbManager.getRecords(handler, first, pageSize, ipFilter);
		} else if ((fromDateFilter != null || toDateFilter != null) && StringUtils.isBlank(ipFilter)) {
			records = dbManager.getRecords(handler, first, pageSize, (fromDateFilter != null ? fromDateFilter.getTime() : 0),
					(toDateFilter != null ? toDateFilter.getTime() : Long.MAX_VALUE));
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
