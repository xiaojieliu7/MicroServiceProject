package librec.intf;

import com.google.common.collect.Table;
import librec.data.ItemContext;
import librec.data.RatingContext;
import librec.data.SparseMatrix;
import librec.data.UserContext;

import java.util.Map;

/**
 * Generic recommenders where contextual information is used. The context can be user-, item- and rating-related.
 * 
 * @author guoguibing
 * 
 */
public class ContextRecommender extends IterativeRecommender {

	// {user, user-context}
	protected static Map<Integer, UserContext> userContexts;
	// {item, item-context}
	protected static Map<Integer, ItemContext> itemContexts;
	// {user, item, rating-context}
	protected static Table<Integer, Integer, RatingContext> ratingContexts;

	// initialization
	static {

		// read context information here
	}

	public ContextRecommender(SparseMatrix trainMatrix, SparseMatrix testMatrix, int fold) {
		super(trainMatrix, testMatrix, fold);
	}
	
}
