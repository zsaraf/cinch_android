package com.seshtutoring.seshapp.view.components;

import com.seshtutoring.seshapp.model.Card;

public class PaymentListItem {

    public static final int HEADER_TYPE = 0;
    public static final int ADD_CARD_TYPE = 1;
    public static final int CARD_TYPE = 2;

    public Card card;
    public int type;
    public String text;

    public PaymentListItem(Card card, int type, String text) {
        this.card = card;
        this.type = type;
        this.text = text;
    }
}