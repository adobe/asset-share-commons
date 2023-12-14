package com.adobe.aem.commons.assetshare.components.details;

import org.osgi.annotation.versioning.ConsumerType;

/**
 *
 * Interface for PDF Component
 *
 */
@ConsumerType
public interface Pdf extends EmptyTextComponent {
    /**
     * @return Returns src of the PDF to render.
     */
    String getSrc();

    /**
     *
     * @return the name (node name - aka file name) of the asset being rendered.
     */
    String getFileName();

    /**
     * @return the Adobe Acrobat DC client ID used to instantiate the PDF viewer.
     */
    String getClientId();

    /**
     * @return Returns a unique ID for this instance of the component. This is not-consistent across page reloads.
     */
    String getViewerId();


    /**
     * @return the mode in which the PDF viewer should be rendered.
     */
    String getEmbedMode();

    /**
     * @return the default view mode of the PDF viewer.
     */
    String getDefaultViewMode();

    /**
     * Cannot be smaller than 100px;
     * @return the height of the PDF viewer in pixels (##px or auto);
     */
    String getHeight();

    /**
     * @return true if the PDF should be rendered in read-only mode, false otherwise.
     */
    boolean isReadOnly();

    /**
     * @return true if the full screen button should be shown, false otherwise.
     */
    boolean showFullScreen();

    /**
     * @return true if the download button should be shown, false otherwise.
     */
    boolean showDownload();

    /**
     * @return true if the print button should be shown, false otherwise.
     */
    boolean showPrint();

    /**
     * @return true if the zoom controls should be shown, false otherwise.
     */
    boolean showZoomControl();

    /**
     * @return true if the bookmark controls should be shown, false otherwise.
     */
    boolean showBookmarks();

    /**
     * @return true if the annotation tools should be shown, false otherwise.
     */
    boolean showAnnotationTools();

    /**
     * @return true is linearization is enabled, false otherwise.
     */
    boolean isLinearizationEnabled();

}
