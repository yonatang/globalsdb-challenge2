package me.yonatan.globals.c2.view;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Getter;
import lombok.Setter;
import me.yonatan.globals.c2.action.DbManager;
import me.yonatan.globals.c2.entity.LogFile;
import me.yonatan.globals.c2.entity.LogRecord;

import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;

@SuppressWarnings("serial")
@Named
public class LogTableBean implements Serializable {

	@Inject
	private Logger log;

	@Inject
	private DbManager dbManager;

	@Setter
	@Getter
	private LogFile logFile;

	private List<LogRecord> records = new ArrayList<LogRecord>();

	private List<LogRecord> unmodifiableRecords = Collections.unmodifiableList(records);

	public List<LogRecord> getRecords() {
		return unmodifiableRecords;
	}

	public void populate() {
		records.clear();
		records.addAll(dbManager.getRecords(logFile.getHandler()));
	}

	public void pollForChanges() {
		// System.out.println("PollForChanges");
		if (FileUtils.isFileNewer(new File(logFile.getFileName()), logFile.getLastUpdated().toDate())) {
			System.out.println("File has changed. Reloading.");
			try {
				dbManager.reloadFile(logFile);
				logFile = dbManager.getFileInfo(logFile.getHandler());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
