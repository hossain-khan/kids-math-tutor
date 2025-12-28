import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import SearchBar from "@/components/SearchBar";

describe("SearchBar", () => {
  it("should render search input with placeholder", () => {
    const onChange = vi.fn();
    const onSearch = vi.fn();

    render(
      <SearchBar
        value=""
        onChange={onChange}
        onSearch={onSearch}
        placeholder="Search..."
      />,
    );

    expect(screen.getByPlaceholderText("Search...")).toBeInTheDocument();
  });

  it("should call onChange when typing in input", async () => {
    const onChange = vi.fn();
    const onSearch = vi.fn();
    const user = userEvent.setup();

    render(<SearchBar value="" onChange={onChange} onSearch={onSearch} />);

    const input = screen.getByTestId("search-input");
    await user.type(input, "addition");

    expect(onChange).toHaveBeenCalledWith("a");
    expect(onChange).toHaveBeenCalledWith("d");
  });

  it("should call onSearch when search button is clicked", async () => {
    const onChange = vi.fn();
    const onSearch = vi.fn();
    const user = userEvent.setup();

    render(
      <SearchBar value="addition" onChange={onChange} onSearch={onSearch} />,
    );

    const searchButton = screen.getByTestId("search-button");
    await user.click(searchButton);

    expect(onSearch).toHaveBeenCalled();
  });

  it("should call onSearch when Enter is pressed", async () => {
    const onChange = vi.fn();
    const onSearch = vi.fn();
    const user = userEvent.setup();

    render(
      <SearchBar value="addition" onChange={onChange} onSearch={onSearch} />,
    );

    const input = screen.getByTestId("search-input");
    await user.click(input);
    fireEvent.keyDown(input, { key: "Enter" });

    expect(onSearch).toHaveBeenCalled();
  });

  it("should show clear button when input has value", () => {
    const onChange = vi.fn();
    const onSearch = vi.fn();

    render(
      <SearchBar value="addition" onChange={onChange} onSearch={onSearch} />,
    );

    expect(
      screen.getByRole("button", { name: /clear search/i }),
    ).toBeInTheDocument();
  });

  it("should not show clear button when input is empty", () => {
    const onChange = vi.fn();
    const onSearch = vi.fn();

    render(<SearchBar value="" onChange={onChange} onSearch={onSearch} />);

    expect(
      screen.queryByRole("button", { name: /clear search/i }),
    ).not.toBeInTheDocument();
  });

  it("should clear input when clear button is clicked", async () => {
    const onChange = vi.fn();
    const onSearch = vi.fn();
    const user = userEvent.setup();

    render(
      <SearchBar value="addition" onChange={onChange} onSearch={onSearch} />,
    );

    const clearButton = screen.getByRole("button", { name: /clear search/i });
    await user.click(clearButton);

    expect(onChange).toHaveBeenCalledWith("");
  });

  it("should disable input and buttons when disabled prop is true", () => {
    const onChange = vi.fn();
    const onSearch = vi.fn();

    render(
      <SearchBar
        value="addition"
        onChange={onChange}
        onSearch={onSearch}
        disabled
      />,
    );

    expect(screen.getByTestId("search-input")).toBeDisabled();
    expect(screen.getByTestId("search-button")).toBeDisabled();
  });

  it("should disable search button when input is empty", () => {
    const onChange = vi.fn();
    const onSearch = vi.fn();

    render(<SearchBar value="" onChange={onChange} onSearch={onSearch} />);

    expect(screen.getByTestId("search-button")).toBeDisabled();
  });

  it("should enable search button when input has value", () => {
    const onChange = vi.fn();
    const onSearch = vi.fn();

    render(
      <SearchBar value="addition" onChange={onChange} onSearch={onSearch} />,
    );

    expect(screen.getByTestId("search-button")).not.toBeDisabled();
  });

  it("should use default placeholder when not provided", () => {
    const onChange = vi.fn();
    const onSearch = vi.fn();

    render(<SearchBar value="" onChange={onChange} onSearch={onSearch} />);

    expect(
      screen.getByPlaceholderText("Search worksheets by title or content..."),
    ).toBeInTheDocument();
  });
});
