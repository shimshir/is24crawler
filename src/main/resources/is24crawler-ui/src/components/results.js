import React from 'react'
import {Component, State, Actions} from 'jumpsuit'
import {Table} from 'react-bootstrap'
import '../css/results.css'

const deepFind = (obj, path) => {
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
};

const ResultsState = State(
    {
        initial: {
            exposes: undefined,
            ordering: {
                field: 'price.value',
                direction: 'ASC'
            }
        },
        setExposes(state, exposes) {
            console.log(exposes);
            return {...state, exposes};
        },
        sortExposes(state, field) {
            const newOrdering = {...state.ordering, direction: state.ordering.direction === 'ASC' ? 'DESC' : 'ASC', field: field};

            const ascSortedExposes = state.exposes.slice().sort(
                (a, b) => {
                    const fieldValueA = deepFind(a, field), fieldValueB = deepFind(b, field);
                    if (fieldValueA > fieldValueB) {
                        return 1;
                    } else if (fieldValueA < fieldValueB) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            );

            const sortedExposes = newOrdering.direction === 'ASC' ? ascSortedExposes : ascSortedExposes.reverse();

            return {...state, exposes: sortedExposes, ordering: newOrdering};
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
                                    <th className="sortable" onClick={e => Actions.sortExposes('price.value')}>Price</th>
                                    <th className="sortable" onClick={e => Actions.sortExposes('roomAmount')}>Rooms</th>
                                    <th className="sortable" onClick={e => Actions.sortExposes('surface')}>m<sup>2</sup></th>
                                    <th className="sortable" onClick={e => Actions.sortExposes('address.region')}>Address</th>
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
