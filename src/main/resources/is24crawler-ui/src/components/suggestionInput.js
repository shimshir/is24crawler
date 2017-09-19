import React from 'react'
import {WithContext as ReactTags} from 'react-tag-input'
import '../css/reactTags.css'
import Utils from '../utils'

class SuggestionInput extends React.Component {

    constructor() {
        super();
        this.state = {
            selectedTags: undefined
        };
    }

    setSelectedTags = (tags) => {
        const prevTags = this.state.selectedTags !== undefined ? this.state.selectedTags.slice() : undefined;
        this.props.onTagsChange(tags, prevTags);
        this.setState({selectedTags: tags});
    };

    handleTagDelete = (i) => {
        const cloned = this.state.selectedTags.slice();
        // NOTE: splice mutates the array
        cloned.splice(i, 1);
        this.setSelectedTags(cloned);
    };

    handleTagAddition = (tag) => {
        const filteredSuggestion = this.props.suggestions.find(suggestion => suggestion.text.toLowerCase().includes(tag.toLowerCase()));
        const selectedTags = this.state.selectedTags !== undefined ? this.state.selectedTags.slice() : [];
        if (filteredSuggestion !== undefined && !selectedTags.includes(filteredSuggestion)) {
            selectedTags.push(filteredSuggestion);
            this.setSelectedTags(selectedTags);
        }
    };

    handleTagDrag = (tag, currPos, newPos) => {
        let cloned = this.state.selectedTags.slice();
        // NOTE: splice mutates the array
        cloned.splice(currPos, 1);
        cloned.splice(newPos, 0, tag);
        this.setSelectedTags(cloned);
    };

    handleFilterSuggestions = (textInputValue, possibleSuggestionsArray) => {
        if (textInputValue === '*') {
            return possibleSuggestionsArray;
        }
        const lowerCaseQuery = Utils.replaceUnicodes(textInputValue.toLowerCase());
        return possibleSuggestionsArray.filter(suggestion =>  Utils.replaceUnicodes(suggestion.toLowerCase()).includes(lowerCaseQuery));
    };

    componentWillMount() {
        this.setSelectedTags(this.state.selectedTags !== undefined ? this.state.selectedTags : this.props.defaultTags);
    }

    render() {
        const tagSuggestions = this.props.suggestions.map(suggestion => suggestion.text);
        return (
            <ReactTags id={this.props.id}
                       tags={this.state.selectedTags}
                       suggestions={tagSuggestions}
                       placeholder={this.props.placeholder}
                       autocomplete={true}
                       minQueryLength={1}
                       handleDelete={this.handleTagDelete}
                       handleAddition={this.handleTagAddition}
                       handleDrag={this.handleTagDrag}
                       handleFilterSuggestions={this.handleFilterSuggestions}
                       classNames={{tagInputField: ' form-control'}}
            />
        );
    }
}

export default SuggestionInput;