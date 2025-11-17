package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    public Invoice invoice;
    public Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {

        int totalAmount = getTotalAmount();

        int volumeCredits = getTotalVolumeCredits();

        StringBuilder result = new StringBuilder("Statement for " + invoice.getCustomer() + System.lineSeparator());

        for (Performance p : invoice.getPerformances()) {
            result.append(String.format(
                    "  %s: %s (%s seats)%n",
                    getPlay(p).name,
                    usd(getAmount(p)),
                    p.getAudience()
            ));
        }

        result.append(String.format("Amount owed is %s%n", usd(totalAmount)));
        result.append(String.format("You earned %s credits%n", volumeCredits));

        return result.toString();
    }

    private int getTotalVolumeCredits() {
        int volumeCredits = 0;
        for (Performance p : invoice.getPerformances()) {
            volumeCredits += getVolumeCredits(p); // we’ll refactor getVolumeCredits to not need the previous total
        }
        return volumeCredits;
    }

    private int getTotalAmount() {
        int totalAmount = 0;
        for (Performance p : invoice.getPerformances()) {
            totalAmount += getAmount(p);
        }
        return totalAmount;
    }

    private static String usd(int totalAmount) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(totalAmount / 100);
    }

    private int getVolumeCredits(Performance performance) {
        int credits = Math.max(performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
        if ("comedy".equals(getPlay(performance).getType())) {
            credits += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }
        return credits;
    }

    private Play getPlay(Performance p) {
        return plays.get(p.playID);
    }

    private int getAmount(Performance performance) {
        Play play = getPlay(performance);
        int result = 0;
        switch (play.getType()) {
            case "tragedy":
                result = 40000;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    result += 1000 * (performance.getAudience() - 30);
                }
                break;
            case "comedy":
                result = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    result += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                result += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", play.getType()));
        }
        return result;
    }
}
