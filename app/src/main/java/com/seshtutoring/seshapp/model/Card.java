package com.seshtutoring.seshapp.model;

import android.util.Log;

import com.orm.SugarRecord;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by franzwarning on 9/8/15.
 */
public class Card extends SugarRecord<Message> {
    private static final String TAG = Card.class.getName();

    private static final String TYPE_KEY = "type";
    private static final String DEFAULT_KEY = "is_default";
    private static final String CARD_ID_KEY = "card_id";
    private static final String DEBIT_KEY = "is_debit";
    private static final String RECIPIENT_KEY = "is_recipient";
    private  static final String LAST_FOUR_KEY = "last_four";

    public String cardId;
    public String type;
    public String lastFour;
    public boolean isDebit;
    public boolean isDefault;
    public boolean isRecipient;
    public User user;

    public static Card createOrUpdateCardWithJSON(JSONObject jsonObject, User user) {

        Card card = null;
        try {

            String cardId = jsonObject.getString(CARD_ID_KEY);
            card = createOrUpdateCardWithId(cardId);

            // Assign all the properties of the message
            card.type = jsonObject.getString(TYPE_KEY);
            card.lastFour = jsonObject.getString(LAST_FOUR_KEY);
            card.isDebit = jsonObject.getBoolean(DEBIT_KEY);
            card.isDefault = jsonObject.getBoolean(DEFAULT_KEY);
            card.isRecipient = jsonObject.getBoolean(RECIPIENT_KEY);

            card.user = user;

        } catch (JSONException e) {
            Log.e(TAG, "Failed to create or update user in db; JSON user object from server is malformed: " + e.getMessage());
            return null;
        }
        return card;
    }

    private static Card createOrUpdateCardWithId(String cardId) {
        Card card = null;
        List<Card> cardsFound = Card.find(Card.class, "card_id = ?", cardId);
        if (cardsFound.size() > 0) {
            card = cardsFound.get(0);
        } else {
            card = new Card();
            card.cardId = cardId;
        }

        return card;
    }

    public static List<Card> getPaymentCards() {
        return Card.find(Card.class, "is_recipient = '0'");
    }

    public static List<Card> getCashoutCards() {
        return Card.find(Card.class, "is_recipient = '1'");
    }


}
