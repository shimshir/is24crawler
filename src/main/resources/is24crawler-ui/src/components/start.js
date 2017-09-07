import React from 'react'
import {Component} from 'jumpsuit'
import {Search} from './search'
import {Results} from './results'

export default Component(
    {
        render() {
            return (
                <div>
                    <Search/>
                    <Results/>
                </div>
            )
        }
    }
);
