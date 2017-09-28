class Utils {
    deepFind(obj, path) {
        let paths = path.split('.')
            , current = obj
            , i;

        for (i = 0; i < paths.length; ++i) {
            if (!current[paths[i]]) {
                return undefined;
            } else {
                current = current[paths[i]];
            }
        }
        return current;
    }
    addressToGoogleMapLink(address) {
        const regionEnc = address.region.trim().split(' ').map(encodeURIComponent).join('+');
        const realRegionCommaIndex = regionEnc.indexOf('%2C');
        const regionCommaIndex = realRegionCommaIndex !== -1 ? realRegionCommaIndex : regionEnc.length;
        const regionEncTrimmed = regionEnc.substring(0, regionCommaIndex);
        const streetEnc = address.street.trim().split(' ').map(encodeURIComponent).join('+');
        // TODO: Move this to the backend
        if (address.street === "Die vollständige Adresse der Immobilie erhalten Sie vom Anbieter.") {
            return `https://www.google.de/maps/place/${regionEncTrimmed}`;
        } else {
            return `https://www.google.de/maps/place/${streetEnc},+${regionEncTrimmed}`;
        }

    }
    replaceAll(str, search, replacement) {
        return str.split(search).join(replacement);
    }
    replaceUnicodes(str) {
        return [['ü', 'u'], ['ä', 'a'], ['ö', 'o'], ['ß', 's']].reduce((acc, replacement) => this.replaceAll(acc, replacement[0], replacement[1]), str);
    }
}

const utils = new Utils();
export default utils;