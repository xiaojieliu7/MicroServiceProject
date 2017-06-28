package librec.intf;

import librec.data.ItemContext;
import librec.data.RatingContext;
import librec.data.SparseMatrix;
import librec.data.UserContext;

/**
 * @author Keqiang Wang
 */
public class IterativeContextRecommender extends IterativeRecommender {

    /**
     * user context entries
     */
    protected static UserContext userContext;

    /**
     * item context entries
     */
    protected static ItemContext itemContext;
    /**
     * rating(user-item pair) element context entries
     */
    protected static RatingContext ratingContext;

    public IterativeContextRecommender(SparseMatrix trainMatrix, SparseMatrix testMatrix, int fold) {
        super(trainMatrix, testMatrix, fold);
    }
}
