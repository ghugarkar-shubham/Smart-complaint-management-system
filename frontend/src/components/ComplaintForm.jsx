import { useState } from 'react';

const initialState = {
  title: '',
  category: '',
  description: '',
  photo: null,
};

export default function ComplaintForm({ onSubmit, loading }) {
  const [form, setForm] = useState(initialState);
  const [errors, setErrors] = useState({});

  const validate = () => {
    const nextErrors = {};
    if (!form.title.trim()) nextErrors.title = 'Title is required';
    if (!form.category.trim()) nextErrors.category = 'Category is required';
    if (!form.description.trim()) nextErrors.description = 'Description is required';
    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!validate()) return;
    const formData = new FormData();
    const data = { title: form.title, category: form.category, description: form.description };
    formData.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }));
    if (form.photo) formData.append('photo', form.photo);
    await onSubmit(formData);
    setForm(initialState);
  };

  return (
    <form className="card form-card" onSubmit={handleSubmit}>
      <div className="section-heading">
        <div>
          <p className="eyebrow">New complaint</p>
          <h2>Submit an issue</h2>
        </div>
      </div>

      <label>
        Title
        <input value={form.title} onChange={(event) => setForm({ ...form, title: event.target.value })} placeholder="Brief complaint title" />
        {errors.title && <span className="field-error">{errors.title}</span>}
      </label>

      <label>
        Category
        <select value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })}>
          <option value="">Select category</option>
          <option>Infrastructure</option>
          <option>Facility</option>
          <option>Security</option>
          <option>Sanitation</option>
          <option>IT Support</option>
          <option>Other</option>
        </select>
        {errors.category && <span className="field-error">{errors.category}</span>}
      </label>

      <label>
        Description
        <textarea value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} rows="5" placeholder="Explain the issue in detail" />
        {errors.description && <span className="field-error">{errors.description}</span>}
      </label>

      <label>
        Photo (optional)
        <input type="file" accept="image/*" onChange={(e) => setForm({ ...form, photo: e.target.files[0] || null })} />
      </label>

      <button className="btn btn-primary" type="submit" disabled={loading}>
        {loading ? 'Submitting...' : 'Submit Complaint'}
      </button>
    </form>
  );
}
