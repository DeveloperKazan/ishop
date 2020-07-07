package net.devstudy.ishop.service;

import net.devstudy.ishop.model.SocialAccount;

public interface SocialService {

	String getAuthorizeUrl();

	SocialAccount getSocialAccount(String authToken);
}
