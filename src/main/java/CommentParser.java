package io.github.qwefgh90.commentparser;

import io.github.qwefgh90.commentparser.Extractor;
import io.github.qwefgh90.commentparser.Extractor.ExtractResult;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Collectors;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;

import scala.Option;
import scala.collection.JavaConverters;

/**
 * CommentParser provides static methods for extracting comments.
 * <br>Supported languages are JAVA, PY, C, CPP, C_HEADER, CPP_HEADER, SCALA, RUBY, GO, JS, HTML, BAT, SH, XML, TEXT, MD, ETC.
 */
public class CommentParser{

    /**
     * CommentResult contains each comment in resource.
     * If a uri is not provided as parameter, a uri is a location of a temporary file.
     */
    public static class CommentResult{
        public final int startOffset;
        public final String comment;
        public final URI uri;
        public CommentResult(int startOffset, String comment, URI uri){
            this.startOffset = startOffset;
            this.comment = comment;
            this.uri = uri;
        }
    }
    
    /**
     * Extract comments from a stream with a resource name. Default charset is utf-8.
     * @param is a source stream
     * @param resourceName a name about a resource name
     * @return a list of <b>CommentResult</b>
     */
    public static Optional<java.util.List<CommentResult>> extractComments(InputStream is, String resourceName){
        return extractComments(is, resourceName, StandardCharsets.UTF_8);
    }
    
    /**
     * Extract comments from a stream with a resource name.
     * @param is a source stream
     * @param resourceName a name of a resource
     * @param charset a charset of a resource
     * @return a list of <b>CommentResult</b>
     */
    public static Optional<java.util.List<CommentResult>> extractComments(InputStream is, String resourceName, Charset charset) {
        Option<scala.collection.immutable.List<ExtractResult>> scalaReturn = Extractor.extractCommentsByStream(is, resourceName, charset);
        return Optional.ofNullable(scalaReturn.map((scala.collection.immutable.List<ExtractResult> list) -> {
                    java.util.List<ExtractResult> javaList = JavaConverters.seqAsJavaList(list);
                    return javaList.stream().map((ExtractResult result) -> {
                            return new CommentResult(result.startOffset(), result.comment(), result.uri());
                        }).collect(Collectors.toList());
                }).getOrElse(null));
    }

    /**
     * Extract comments from a uri with a resource name.
     * @param uri a source uri
     * @param resourceName a name of a resource
     * @return a list of <b>CommentResult</b>
     */
    public static Optional<java.util.List<CommentResult>> extractComments(URI uri, String resourceName) {
        return extractComments(uri, resourceName, StandardCharsets.UTF_8);
    }
    
    /**
     * Extract comments from a uri with a resource name. Default charset is utf-8.
     * @param uri a source uri
     * @param resourceName a name of a resource
     * @param charset a charset of a resource
     * @return a list of <b>CommentResult</b>
     */
    public static Optional<java.util.List<CommentResult>> extractComments(URI uri, String resourceName, Charset charset) {
        //StandardCharsets.UTF_8
        Option<scala.collection.immutable.List<ExtractResult>> scalaReturn = Extractor.extractComments(uri, resourceName, charset);
        return Optional.ofNullable(scalaReturn.map((scala.collection.immutable.List<ExtractResult> list) -> {
                    java.util.List<ExtractResult> javaList = JavaConverters.seqAsJavaList(list);
                    return javaList.stream().map((ExtractResult result) -> {
                            return new CommentResult(result.startOffset(), result.comment(), result.uri());
                        }).collect(Collectors.toList());
                }).getOrElse(null));
    }
}
