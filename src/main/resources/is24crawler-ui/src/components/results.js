import React from 'react'
import {Component, State} from 'jumpsuit'
import {Table} from 'react-bootstrap'

const ResultsState = State(
    {
        initial: {},
        setExposes(state, exposes) {
            console.log(exposes);
            return {...state, exposes};
        }
    }
);

const addressToGoogleMapLink = address => {
    const regionEnc = address.region.trim().split(' ').join('+');
    const realRegionCommaIndex = regionEnc.indexOf(',');
    const regionCommaIndex = realRegionCommaIndex !== -1 ? realRegionCommaIndex : regionEnc.length;
    const regionEncTrimmed = regionEnc.substring(0, regionCommaIndex);
    const streetEnc = address.street.trim().split(' ').join('+');
    // TODO: Move this to the backend
    if (address.street === "Die vollständige Adresse der Immobilie erhalten Sie vom Anbieter.") {
        return `https://www.google.de/maps/place/${regionEncTrimmed}`;
    } else {
        return `https://www.google.de/maps/place/${streetEnc},+${regionEncTrimmed}`;
    }

};

const Results = Component(
    {
        render() {

            const exposes = this.props.exposes;
            if (exposes) {
                if (exposes.length === 0) {
                    return (<div>No results found</div>);
                } else {
                    return (
                        <div>
                            <br/>
                            <Table responsive hover>
                                <thead>
                                <tr>
                                    <th>Price</th>
                                    <th>Rooms</th>
                                    <th>m<sup>2</sup></th>
                                    <th>Address</th>
                                    <th>Link</th>
                                </tr>
                                </thead>
                                <tbody>
                                {
                                    exposes.map(
                                        (expose, index) =>
                                            <tr key={index}>
                                                <td>{expose.price.string}</td>
                                                <td>{expose.roomAmount}</td>
                                                <td>{Math.round(expose.surface * 100) / 100}</td>
                                                <td>
                                                    <a href={addressToGoogleMapLink(expose.address)}>
                                                        {expose.address.region}, {expose.address.street}
                                                        </a>
                                                </td>
                                                <td><a href={expose.pageLink}>View</a></td>
                                            </tr>
                                    )
                                }
                                </tbody>
                            </Table>
                        </div>
                    )
                }
            } else {
                return null;
            }
        }
    }, (state) => state.results
);

export {Results, ResultsState};
