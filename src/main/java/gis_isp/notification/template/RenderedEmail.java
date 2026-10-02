package gis_isp.notification.template;

// Result of rendering a template: HTML body plus its plain-text twin.
public record RenderedEmail(String html, String text) {}