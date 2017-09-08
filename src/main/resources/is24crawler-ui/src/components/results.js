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
                            <Table responsive bordered hover>
                                <thead>
                                <tr>
                                    <th>Price</th>
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
                                                <td>{expose.address.region}, {expose.address.street}</td>
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
