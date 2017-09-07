import React from 'react'
import {Component, State} from 'jumpsuit'

const ResultsState = State(
    {
        initial: {
            exposes: []
        },
        setExposes(state, exposes) {
            console.log(exposes);
            return {exposes};
        }
    }
);

const Results = Component(
    {
        render() {
            return (
                <div>
                    Results
                    <ul>
                        {
                            this.props.exposes.map(
                                (expose, index) =>
                                    <li key={index}>{expose.price.string};{expose.address.region}</li>
                            )
                        }
                    </ul>
                </div>
            )
        }
    }, (state) => state.results
);

export {Results, ResultsState};
