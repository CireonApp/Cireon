package com.cireonapp.server.domain.media.common;

public record SearchResults<Content>(Content content, int score) {
}
