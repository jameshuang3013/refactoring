package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter
{
    private final Invoice invoice;
    private final Map<String, Play> plays;

    /**
     * Constructs a StatementPrinter for the given invoice and plays.
     *
     * @param invoice the invoice containing performances
     * @param plays   the map of play IDs to Play objects
     */
    public StatementPrinter(final Invoice invoice, final Map<String, Play> plays)
    {
        this.invoice = invoice;
        this.plays = plays;
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     *
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement()
    {
        final int totalAmount = getTotalAmount();
        final int volumeCredits = getTotalVolumeCredits();
        final StringBuilder result = new StringBuilder("Statement for " + invoice.getCustomer() + System.lineSeparator());

        for (Performance performance : invoice.getPerformances())
        {
            result.append(String.format(
                    "  %s: %s (%s seats)%n",
                    getPlay(performance).getName(),
                    usd(getAmount(performance)),
                    performance.getAudience()
            ));
        }

        result.append(String.format("Amount owed is %s%n", usd(totalAmount)));
        result.append(String.format("You earned %s credits%n", volumeCredits));

        return result.toString();
    }

    private int getTotalVolumeCredits()
    {
        int totalCredits = 0;
        for (Performance performance : invoice.getPerformances())
        {
            totalCredits += getVolumeCredits(performance);
        }
        return totalCredits;
    }

    private int getTotalAmount()
    {
        int totalAmount = 0;
        for (Performance performance : invoice.getPerformances())
        {
            totalAmount += getAmount(performance);
        }
        return totalAmount;
    }

    private static String usd(final int amountInCents)
    {
        return NumberFormat.getCurrencyInstance(Locale.US).format(amountInCents / 100);
    }

    private int getVolumeCredits(final Performance performance)
    {
        int credits = Math.max(performance.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
        if ("comedy".equals(getPlay(performance).getType()))
        {
            credits += performance.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
        }
        return credits;
    }

    private Play getPlay(final Performance performance)
    {
        return plays.get(performance.getPlayID());
    }

    private int getAmount(final Performance performance)
    {
        final Play play = getPlay(performance);
        int amount = 0;

        switch (play.getType())
        {
            case "tragedy":
                amount = Constants.TRAGEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD)
                {
                    amount += Constants.TRAGEDY_AUDIENCE_OVER_AMOUNT
                            * (performance.getAudience() - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;

            case "comedy":
                amount = Constants.COMEDY_BASE_AMOUNT;
                if (performance.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD)
                {
                    amount += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (performance.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD);
                }
                amount += Constants.COMEDY_AMOUNT_PER_AUDIENCE * performance.getAudience();
                break;

            default:
                throw new RuntimeException(String.format("unknown type: %s", play.getType()));
        }

        return amount;
    }
}
