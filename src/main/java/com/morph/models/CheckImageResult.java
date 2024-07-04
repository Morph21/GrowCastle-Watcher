package com.morph.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CheckImageResult {
    private List<Boolean> foundInImages;
    private String resultsInImage;
    private BigDecimal blackRatio;

    private boolean imagesAreTheSame;

    public boolean isImagesAreTheSame() {
        return imagesAreTheSame;
    }

    public void setImagesAreTheSame(boolean imagesAreTheSame) {
        this.imagesAreTheSame = imagesAreTheSame;
    }


    public List<Boolean> getFoundInImages() {
        return foundInImages;
    }

    public void setFoundInImages(List<Boolean> foundInImages) {
        this.foundInImages = foundInImages;
    }

    public void add(boolean src) {
        if (foundInImages == null) {
            foundInImages = new ArrayList<>();
        }

        foundInImages.add(src);
    }

    public String getResultsInImage() {
        return resultsInImage;
    }

    public void setResultsInImage(String resultsInImage) {
        this.resultsInImage = resultsInImage;
    }

    public BigDecimal getBlackRatio() {
        return blackRatio;
    }

    public void setBlackRatio(BigDecimal blackRatio) {
        this.blackRatio = blackRatio;
    }
}
