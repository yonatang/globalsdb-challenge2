package me.yonatan.globals.c2.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import lombok.Cleanup;
import me.yonatan.globals.c2.entity.LogFile;
import me.yonatan.globals.c2.entity.LogRecord;
import me.yonatan.globals.c2.parser.LogParser;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.joda.time.DateTime;

import com.google.common.io.LineReader;
import com.google.gson.Gson;
import com.intersys.globals.Connection;
import com.intersys.globals.NodeReference;

@Named
@ApplicationScoped
public class DbManager {

	private final Object[] FILE_NAME = new Object[] { "file", "name" };
	private final Object[] FILE_TIMESTAMP = new Object[] { "file", "timestamp" };
	private final String LOGS = "logs";

	@Inject
	private Logger log;

	@Inject
	private Connection connection;

	@Inject
	private Gson gson;

	@Inject
	private LogParser logParser;

	public String importLocalFile(File file) throws IOException {
		log.infov("Opening file {0}", file);
		if (!file.isFile()) {
			throw new IOException("File " + file + " couldn't be used");
		}

		String handler = "l" + StringUtils.substring(DigestUtils.shaHex(file.getCanonicalPath()), 0, 29);
		System.out.println("Creating hadnler " + handler);
		@Cleanup
		NodeReference nr = connection.createNodeReference(handler);
		nr.kill();
		nr.set(file.getAbsolutePath(), FILE_NAME);
		nr.set(file.lastModified(), FILE_TIMESTAMP);

		@Cleanup
		FileReader fileReader = new FileReader(file);
		LineReader lineReader = new LineReader(fileReader);

		String line = null;
		while ((line = lineReader.readLine()) != null) {
			long id = nr.increment(1, LOGS);
			LogRecord lr = logParser.parse(line);
			lr.setId(id);
			if (lr != null) {
				String json = gson.toJson(lr);
				nr.set(json, LOGS, id);
			}
		}

		return handler;

	}

	public void reloadFile(LogFile logFile) throws IOException {
		File file = logFile.getFile();
		// TODO
		if (file == null)
			return;

		@Cleanup
		NodeReference nr = connection.createNodeReference(logFile.getHandler());

		@Cleanup
		FileReader fileReader = new FileReader(file);
		LineReader lineReader = new LineReader(fileReader);

		String lastJson = nr.getString(LOGS, nr.getLong(LOGS));
		LogRecord lastRecord = gson.fromJson(lastJson, LogRecord.class);
		boolean updated = false;

		String line = null;
		while ((line = lineReader.readLine()) != null) {
			LogRecord lr = logParser.parse(line);
			if (updated || lr.getTimestamp().isAfter(lastRecord.getTimestamp())) {
				long id = nr.increment(1, LOGS);
				lr.setId(id);
				if (lr != null) {
					String json = gson.toJson(lr);
					nr.set(json, LOGS, id);
				}
				updated = true;
			}
		}
		nr.set(file.lastModified(), FILE_TIMESTAMP);
		System.out.println("Updated!");

	}

	public List<LogRecord> getRecords(String handler) {
		@Cleanup
		NodeReference nr = connection.createNodeReference(handler);
		System.out.println("Loading data for file " + nr.getString(FILE_NAME));
		System.out.println("Last updated at " + new DateTime(nr.getLong(FILE_TIMESTAMP)));
		String next = nr.nextSubscript(LOGS, "");
		ArrayList<LogRecord> records = new ArrayList<LogRecord>();
		while (StringUtils.isNotEmpty(next)) {
			String json = nr.getString(LOGS, next);
			LogRecord lr = gson.fromJson(json, LogRecord.class);
			records.add(lr);
			next = nr.nextSubscript(LOGS, next);
		}

		return records;
	}

	public LogFile getFileInfo(String handler) {
		@Cleanup
		NodeReference nr = connection.createNodeReference(handler);

		return new LogFile(nr.getString(FILE_NAME), new DateTime(nr.getLong(FILE_TIMESTAMP)), handler);
	}
}
