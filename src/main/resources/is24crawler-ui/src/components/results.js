import React from 'react'
import {Component, State, Actions} from 'jumpsuit'
import {Table} from 'react-bootstrap'
import '../css/results.css'
import Utils from '../utils'

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
            return {...state, exposes};
        },
        sortExposes(state, field) {
            const newOrdering = {...state.ordering, direction: state.ordering.direction === 'ASC' ? 'DESC' : 'ASC', field: field};

            const ascSortedExposes = state.exposes.slice().sort(
                (a, b) => {
                    const fieldValueA = Utils.deepFind(a, field), fieldValueB = Utils.deepFind(b, field);
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

const getSortingClassName = (field, ordering) => {
    return ordering.field !== field ? 'fa-sort' : ordering.direction === 'ASC' ? 'fa-sort-asc' : 'fa-sort-desc'
};

const Results = Component(
    {
        render() {
            const exposes = this.props.exposes;
            const ordering = this.props.ordering;
            const sortingClassNames = {
                price: getSortingClassName('price.value', ordering),
                rooms: getSortingClassName('roomAmount', ordering),
                surface: getSortingClassName('surface', ordering),
                address: getSortingClassName('address.region', ordering)
            };
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
                                    <th className="sortable" onClick={e => Actions.sortExposes('price.value')}>
                                        Price
                                        <i className={`fa fa-fw ${sortingClassNames.price}`}/>
                                    </th>
                                    <th className="sortable" onClick={e => Actions.sortExposes('roomAmount')}>
                                        Rooms
                                        <i className={`fa fa-fw ${sortingClassNames.rooms}`}/>
                                    </th>
                                    <th className="sortable" onClick={e => Actions.sortExposes('surface')}>
                                        m<sup>2</sup>
                                        <i className={`fa fa-fw ${sortingClassNames.surface}`}/>
                                    </th>
                                    <th className="sortable" onClick={e => Actions.sortExposes('address.region')}>
                                        Address
                                        <i className={`fa fa-fw ${sortingClassNames.address}`}/>
                                    </th>
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
                                                    <a href={Utils.addressToGoogleMapLink(expose.address)}>
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
