package com.jvm_bloggers.frontend.public_area.blogs;

import com.jvm_bloggers.domain.query.blog_post_for_listing.BlogDisplayDetails;
import com.jvm_bloggers.domain.query.blog_post_for_listing.BlogPostForListing;
import com.jvm_bloggers.entities.blog.BlogType;
import com.jvm_bloggers.frontend.common_components.infinite_scroll.InfinitePaginationPanel;
import com.jvm_bloggers.frontend.public_area.AbstractFrontendPage;

import javaslang.control.Option;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.annotation.mount.MountPath;

import static com.jvm_bloggers.frontend.public_area.blogs.BlogPostsPage.BLOG_BOOKMARKABLE_ID_PARAM;
import static com.jvm_bloggers.utils.DateTimeUtilities.DATE_FORMATTER;
import static javaslang.API.$;
import static javaslang.API.Case;
import static javaslang.API.Match;
import static javaslang.Patterns.None;
import static javaslang.Patterns.Some;

@MountPath("blog/${" + BLOG_BOOKMARKABLE_ID_PARAM + "}")
public class BlogPostsPage extends AbstractFrontendPage {

    static final String BACK_LINK = "backLink";
    static final String BLOG_BOOKMARKABLE_ID_PARAM = "blogBookmarkableId";
    static final String BLOG_LINK = "blogLink";
    static final String DATA_VIEW_ID = "pageable";
    static final String DATA_VIEW_WRAPPER_ID = "pageable-wrapper";
    static final String INFINITE_SCROLL_ID = "infinite-pager";
    static final String LINK_ID = "link";
    static final String PUBLISHED_DATE_ID = "date";

    @SpringBean
    BlogPostsPageBackingBean backingBean;

    private Option<BlogDisplayDetails> blogDisplayDetails;

    @Override
    protected String getPageTitle() {
        return "Lista wpisów";
    }

    public BlogPostsPage(PageParameters parameters) {
        BlogPostsPageRequestHandler requestHandler = backingBean
            .requestHandler(parameters.get(BLOG_BOOKMARKABLE_ID_PARAM).toString(""));
        blogDisplayDetails = backingBean.findBlogDisplayDetails(requestHandler.getBlogId());
        add(new ExternalLink(BLOG_LINK, getBlogUrl(), getAuthor()));
        add(createBackLink());
        DataView dataView = createBlogPostDataView(requestHandler);
        WebMarkupContainer pageableWrapper = new WebMarkupContainer(DATA_VIEW_WRAPPER_ID);
        pageableWrapper.setOutputMarkupId(true);
        add(pageableWrapper);
        pageableWrapper.add(dataView);
        pageableWrapper.add(new InfinitePaginationPanel(INFINITE_SCROLL_ID, dataView));
    }

    private DataView<BlogPostForListing> createBlogPostDataView(
        BlogPostsPageRequestHandler requestHandler) {
        final DataView<BlogPostForListing> dataView =
            new DataView<BlogPostForListing>(DATA_VIEW_ID, requestHandler) {
                @Override
                protected void populateItem(Item<BlogPostForListing> item) {
                    BlogPostForListing post = item.getModelObject();
                    item.add(new ExternalLink(LINK_ID,
                            backingBean.generateRedirectLink(post), post.getTitle()));
                    item.add(new Label(PUBLISHED_DATE_ID,
                        post.getPublishedDate().format(DATE_FORMATTER)));
                }
            };

        dataView.setItemsPerPage(backingBean.defaultPageSize());
        dataView.setOutputMarkupId(true);
        return dataView;
    }

    private String getAuthor() {
        return blogDisplayDetails
            .map(BlogDisplayDetails::getAuthor)
            .getOrElse("John Doe");
    }

    private String getBlogUrl() {
        return blogDisplayDetails
            .map(BlogDisplayDetails::getUrl)
            .getOrElse("#");
    }

    private BookmarkablePageLink createBackLink() {
        return Match(blogDisplayDetails.map(BlogDisplayDetails::getType)).of(
            Case(Some($(BlogType.PERSONAL)),
                new BookmarkablePageLink(BACK_LINK, PersonalBlogsPage.class)),
            Case(Some($(BlogType.COMPANY)),
                new BookmarkablePageLink(BACK_LINK, CompanyBlogsPage.class)),
            Case(Some($(BlogType.VIDEOS)),
                new BookmarkablePageLink(BACK_LINK, VideoBlogsPage.class)),
            Case(None(), new BookmarkablePageLink(BACK_LINK, PersonalBlogsPage.class))
        );
    }
}