package me.yonatan.globals.c2.view;

import java.io.File;
import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;

import lombok.Getter;
import lombok.Synchronized;
import me.yonatan.globals.c2.action.DbManager;
import me.yonatan.globals.c2.entity.LogFile;

import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;

@SuppressWarnings("serial")
@Named
public class LogTableBean implements Serializable {

	@Inject
	private Logger log;

	@Inject
	private DbManager dbManager;

	@Inject
	@Getter
	private LogTableDataModel dataModel;

	@Getter
	private LogFile logFile;

	public void setLogFile(LogFile logFile) {
		String handler = logFile.getHandler();
		dataModel.setHandler(logFile.getHandler());
		dataModel.setRowCount(dbManager.getLogCountInt(handler));
		this.logFile = logFile;
	}

	@Synchronized
	public void pollForChanges() {
		// TODO push changes?
		if (FileUtils.isFileNewer(new File(logFile.getFileName()), logFile.getLastUpdated().toDate())) {
			System.out.println("File has changed. Reloading.");
			dbManager.reloadFile(logFile);
			setLogFile(dbManager.getFileInfo(logFile.getHandler()));

		}
	}

}
