package ru.rrusanov.notes.domain.jsonmapper;

import java.util.Date;
import java.util.Objects;

/**
 * @author Roman Rusanov
 * @since 22.06.2021
 * email roman9628@gmail.com
 * Class describe instance to parse json from request.
 */
public class JsonDate {

    private Date date;
    private Character comparisonSign;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Character getComparisonSign() {
        return comparisonSign;
    }

    public void setComparisonSign(Character comparisonSign) {
        this.comparisonSign = comparisonSign;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonDate jsonDate = (JsonDate) o;
        return date.equals(jsonDate.date) && comparisonSign.equals(jsonDate.comparisonSign);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, comparisonSign);
    }

    @Override
    public String toString() {
        return "JsonDate{" +
                "date=" + date +
                ", comparisonSign='" + comparisonSign + '\'' +
                '}';
    }
}