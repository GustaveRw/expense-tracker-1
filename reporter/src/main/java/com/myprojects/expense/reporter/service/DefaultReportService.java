package com.myprojects.expense.reporter.service;

import com.myprojects.expense.reporter.dao.DayReportDao;
import com.myprojects.expense.reporter.exception.ReportNotFoundException;
import com.myprojects.expense.reporter.model.DayReport;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DefaultReportService implements ReportService {

    private final DayReportDao dayReportDao;

    public DefaultReportService(DayReportDao dayReportDao) {
        this.dayReportDao = dayReportDao;
    }

    /**
     * Queries the day reports by the given date and returns one, If none found, it throws
     * a {@link ReportNotFoundException}.
     */
    @Override
    public DayReport getDayReport(LocalDate date) {
        DayReport dayReportProbe = new DayReport();
        dayReportProbe.setDate(date);
        return dayReportDao.findOne(Example.of(dayReportProbe))
                .orElseThrow(() -> new ReportNotFoundException());
    }

    /**
     * Queries the day reports by the given date and returns one, If none found, it creates an empty one,
     * stores it in the database, and returns it to the caller.
     */
    @Override
    public DayReport getDayReportOrCreate(LocalDate date) {
        try {
            return getDayReport(date);
        } catch (ReportNotFoundException ex) {
            try {
                dayReportDao.save(DayReport.emptyReport(date));
            } catch (DuplicateKeyException e) {
                // Report already exists, this can happen in case another thread or reporter instance is aggregating
                // expenses or modifying the same report.
            }
            return getDayReport(date);
        }
    }

}
